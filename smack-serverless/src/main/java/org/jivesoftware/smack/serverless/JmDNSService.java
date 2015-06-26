/**
 *
 * Copyright 2015 Ishan Khanna
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
package org.jivesoftware.smack.serverless;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;
import org.jxmpp.jid.BareJid;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.impl.JmDNSImpl;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JmDNSService extends LLService implements ServiceListener {

    static final String SERVICE_TYPE = "_presence._tcp.local.";
    static JmDNS jmDNS = null;

    static {
        try {
            if (jmDNS == null) {
                jmDNS = JmDNS.create();
            }
        }
        catch (IOException e) {
        }
    }

    private ServiceInfo serviceInfo;

    public JmDNSService() {

    }

    @Override public List<BareJid> getAllClientsPresentOnLLNetwork() {
        return null;
    }

    @Override public void announcePresence(LLPresence llPresence) throws XMPPException {

        serviceInfo = ServiceInfo.create(SERVICE_TYPE, presence.getServiceName(), presence.getPort(), 0, 0,
                        presence.toMap());
        jmDNS.addServiceListener(SERVICE_TYPE, this);
        try {
            String originalServiceName = serviceInfo.getName();
            jmDNS.registerService(serviceInfo);
            String realizedServiceName = getRealizedServiceName(serviceInfo);
            presence.setServiceName(realizedServiceName);

            if (!originalServiceName.equals(realizedServiceName)) {
                serviceNameChanged(realizedServiceName, originalServiceName);
            }
        }
        catch (IOException ioe) {
            throw new XMPPException.XMPPErrorException("Failed to register DNS-SD Service",
                            new XMPPError(XMPPError.Condition.undefined_condition), ioe);
        }
    }

    @Override public void concealPresence() {

        if (serviceInfo != null) {
            jmDNS.unregisterService(serviceInfo);
        }

    }

    public void serviceNameChanged(String newName, String oldName) {
        try {
            super.serviceNameChanged(newName, oldName);
        }
        catch (Throwable t) {
            // ignore
        }
    }

    /**
     * JmDNS may change the name of a requested service to enforce uniqueness
     * within its DNS cache. This helper method can be called after {@link javax.jmdns.JmDNS#registerService(javax.jmdns.ServiceInfo)}
     * with the passed {@link javax.jmdns.ServiceInfo} to attempt to determine the actual service
     * name registered. e.g: "test@example" may become "test@example (2)"
     *
     * @param requestedInfo the ServiceInfo instance passed to {@link javax.jmdns.JmDNS#registerService(javax.jmdns.ServiceInfo)}
     * @return the unique service name actually being advertised by JmDNS. If no
     * match found, return requestedInfo.getName()
     */
    private String getRealizedServiceName(ServiceInfo requestedInfo) {
        Map<String, ServiceInfo> map = ((JmDNSImpl) jmDNS).getServices();
        // Check if requested service name is used verbatim
        if (map.containsKey(requestedInfo.getKey())) {
            return map.get(requestedInfo.getKey()).getName();
        }

        // The service name was altered... Search registered services
        // e.g test@example.presence._tcp.local would match test@example (2).presence._tcp.local
        for (ServiceInfo info : map.values()) {
            if (info.getName().contains(requestedInfo.getName()) && info.getTypeWithSubtype().equals(
                            requestedInfo.getTypeWithSubtype())) {
                return info.getName();
            }
        }

        // No match found! Return expected name
        return requestedInfo.getName();
    }

    @Override public void serviceAdded(ServiceEvent event) {

    }

    @Override public void serviceRemoved(ServiceEvent event) {

    }

    @Override public void serviceResolved(ServiceEvent event) {

    }
}
