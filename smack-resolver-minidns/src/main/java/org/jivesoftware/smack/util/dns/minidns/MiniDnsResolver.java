/**
 *
 * Copyright 2014-2016 Florian Schmaus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.smack.util.dns.minidns;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.ConnectionConfiguration.DnssecMode;
import org.jivesoftware.smack.initializer.SmackInitializer;
import org.jivesoftware.smack.util.DNSUtil;
import org.jivesoftware.smack.util.dns.DNSResolver;
import org.jivesoftware.smack.util.dns.HostAddress;
import org.jivesoftware.smack.util.dns.SRVRecord;

import de.measite.minidns.DNSCache;
import de.measite.minidns.cache.LRUCache;
import de.measite.minidns.dnssec.DNSSECClient;
import de.measite.minidns.hla.ResolverApi;
import de.measite.minidns.hla.ResolverResult;
import de.measite.minidns.record.A;
import de.measite.minidns.record.AAAA;
import de.measite.minidns.record.SRV;
import de.measite.minidns.recursive.ReliableDNSClient;


/**
 * This implementation uses the <a href="https://github.com/rtreffer/minidns/">MiniDNS</a> implementation for
 * resolving DNS addresses.
 */
public class MiniDnsResolver extends DNSResolver implements SmackInitializer {

    private static final MiniDnsResolver INSTANCE = new MiniDnsResolver();

    private static final DNSCache CACHE = new LRUCache(128);

    private static final ResolverApi DNSSEC_RESOLVER = new ResolverApi(new DNSSECClient(CACHE));

    private static final ResolverApi NON_DNSSEC_RESOLVER = new ResolverApi(new ReliableDNSClient(CACHE));

    public static DNSResolver getInstance() {
        return INSTANCE;
    }

    public MiniDnsResolver() {
        super(true);
    }

    @Override
    protected List<SRVRecord> lookupSRVRecords0(String name, List<HostAddress> failedAddresses, DnssecMode dnssecMode) {
        final ResolverApi resolver = getResolver(dnssecMode);

        ResolverResult<SRV> result;
        try {
            result = resolver.resolve(name, SRV.class);
        } catch (IOException e) {
            return null;
        }

        if (!result.wasSuccessful()) {
            // TODO should use result.throwIfErrorResponse() here once the method is public and throws a subclass of IOException.
            return Collections.emptyList();
        }

        ensureAuthenticIfDnssecRequested(dnssecMode, result);
 
        List<SRVRecord> res = new LinkedList<SRVRecord>();
        for (SRV srv : result.getAnswers()) {
            String hostname = srv.name.ace;
            HostAddress hostAddress;
            try {
                hostAddress = lookupHostAddress0(hostname, failedAddresses, dnssecMode);
            } catch (IOException e) {
                continue;
            }
            SRVRecord srvRecord = new SRVRecord(hostname, srv.port, srv.priority, srv.weight, hostAddress.getInetAddresses());
            res.add(srvRecord);
        }

        return res;
    }

    @Override
    protected HostAddress lookupHostAddress0(String name, List<HostAddress> failedAddresses, DnssecMode dnssecMode) {
        final ResolverApi resolver = getResolver(dnssecMode);

        final ResolverResult<A> aResult;
        final ResolverResult<AAAA> aaaaResult;

        try {
            aResult = resolver.resolve(name, A.class);
            aaaaResult = resolver.resolve(name, AAAA.class);
        } catch (IOException e) {
            
            return null;
        }

        if (!aResult.wasSuccessful() && !aaaaResult.wasSuccessful()) {
            // Both results where not successful.
            // TODO: Report the reasons.
            return null;
        }

        if (shouldAbortBecauseNotAuthentic(dnssecMode, aResult)
                        || shouldAbortBecauseNotAuthentic(dnssecMode, aaaaResult)) {
            return null;
        }

        List<InetAddress> inetAddresses = new ArrayList<>(aResult.getAnswers().size()
                        + aaaaResult.getAnswers().size());

        for (A a : aResult.getAnswers()) {
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByAddress(a.getIp());
            }
            catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            inetAddresses.add(inetAddress);
        }
        for (AAAA aaaa : aaaaResult.getAnswers()) {
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByAddress(name, aaaa.getIp());
            }
            catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            inetAddresses.add(inetAddress);
        }

        return new HostAddress(name, inetAddresses);
    }

    public static void setup() {
        DNSUtil.setDNSResolver(getInstance());
    }

    @Override
    public List<Exception> initialize() {
        setup();
        return null;
    }

    private static ResolverApi getResolver(DnssecMode dnssecMode) {
        if (dnssecMode == DnssecMode.disabled) {
            return NON_DNSSEC_RESOLVER;
        } else {
            return DNSSEC_RESOLVER;
        }
    }

    private static boolean shouldAbortBecauseNotAuthentic(DnssecMode dnssecMode,
                    ResolverResult<?> result) {
        switch (dnssecMode) {
        case needsDnssec:
        case needsDnssecAndDane:
            // Check if the result is authentic data, i.e. there a no reasons the result is unverified.
            if (!result.isAuthenticData()) {
                return true;
            }
            break;
        case disabled:
            break;
        default:
            throw new IllegalStateException("Unknown DnssecMode: " + dnssecMode);
        }
        return false;
    }

}
