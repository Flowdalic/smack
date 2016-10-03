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
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.initializer.SmackInitializer;
import org.jivesoftware.smack.util.DNSUtil;
import org.jivesoftware.smack.util.dns.DNSResolver;
import org.jivesoftware.smack.util.dns.SRVRecord;

import de.measite.minidns.DNSCache;
import de.measite.minidns.cache.LRUCache;
import de.measite.minidns.hla.ResolverApi;
import de.measite.minidns.hla.ResolverResult;
import de.measite.minidns.record.SRV;
import de.measite.minidns.recursive.ReliableDNSClient;


/**
 * This implementation uses the <a href="https://github.com/rtreffer/minidns/">MiniDNS</a> implementation for
 * resolving DNS addresses.
 */
public class MiniDnsResolver implements SmackInitializer, DNSResolver {

    private static final MiniDnsResolver INSTANCE = new MiniDnsResolver();

    private static final DNSCache CACHE = new LRUCache(128);

//    private static final ResolverApi DNSSEC_RESOLVER = new ResolverApi(new DNSSECClient(CACHE));

    private static final ResolverApi NON_DNSSEC_RESOLVER = new ResolverApi(new ReliableDNSClient(CACHE));

    public static DNSResolver getInstance() {
        return INSTANCE;
    }

    @Override
    public List<SRVRecord> lookupSRVRecords(String name) throws IOException {
        final ResolverApi resolver = NON_DNSSEC_RESOLVER;
        ResolverResult<SRV> result = resolver.resolve(name, SRV.class);
        List<SRVRecord> res = new LinkedList<SRVRecord>();

        if (!result.wasSuccessful()) {
            // TODO should use result.throwIfErrorResponse() here once the method is public and throws a subclass of IOException.
            return res;
        }

        for (SRV srv : result.getAnswers()) {
            res.add(new SRVRecord(srv.name.toString(), srv.port, srv.priority, srv.weight));
        }
        return res;
    }

    public static void setup() {
        DNSUtil.setDNSResolver(getInstance());
    }

    @Override
    public List<Exception> initialize() {
        setup();
        return null;
    }

}
