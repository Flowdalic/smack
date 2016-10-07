/**
 *
 * Copyright 2013-2016 Florian Schmaus
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
package org.jivesoftware.smack.util.dns;

import java.util.List;

import org.jivesoftware.smack.ConnectionConfiguration.DnssecMode;

/**
 * Implementations of this interface define a class that is capable of resolving DNS addresses.
 *
 */
public abstract class DNSResolver {

    private final boolean supportsDnssec;

    protected DNSResolver(boolean supportsDnssec) {
        this.supportsDnssec = supportsDnssec;
    }

    
    /**
     * Gets a list of service records for the specified service.
     * @param name The symbolic name of the service.
     * @return The list of SRV records mapped to the service name.
     */
    public List<SRVRecord> lookupSRVRecords(String name, List<HostAddress> failedAddresses, DnssecMode dnssecMode) {
        checkIfDnssecRequestedAndSupported(dnssecMode);
        return lookupSRVRecords0(name, failedAddresses, dnssecMode);
    }

    protected abstract List<SRVRecord> lookupSRVRecords0(String name, List<HostAddress> failedAddresses, DnssecMode dnssecMode);

    public HostAddress lookupHostAddress(String name, List<HostAddress> failedAddresses, DnssecMode dnssecMode) {
        checkIfDnssecRequestedAndSupported(dnssecMode);
        return lookupHostAddress0(name, failedAddresses, dnssecMode);
    }

    protected abstract HostAddress lookupHostAddress0(String name, List<HostAddress> failedAddresses, DnssecMode dnssecMode);

    private final void checkIfDnssecRequestedAndSupported(DnssecMode dnssecMode) {
        if (dnssecMode != DnssecMode.disabled && !supportsDnssec) {
            throw new UnsupportedOperationException("This resolver does not support DNSSEC");
        }
    }
}
