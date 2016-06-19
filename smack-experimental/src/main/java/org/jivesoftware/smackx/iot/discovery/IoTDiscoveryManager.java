/**
 *
 * Copyright 2016 Florian Schmaus
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
package org.jivesoftware.smackx.iot.discovery;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.jivesoftware.smack.Manager;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.iot.Thing;
import org.jivesoftware.smackx.iot.discovery.element.Constants;
import org.jivesoftware.smackx.iot.discovery.element.IoTClaimed;
import org.jivesoftware.smackx.iot.discovery.element.IoTDisown;
import org.jivesoftware.smackx.iot.discovery.element.IoTMine;
import org.jivesoftware.smackx.iot.discovery.element.IoTRegister;
import org.jivesoftware.smackx.iot.discovery.element.IoTRemove;
import org.jivesoftware.smackx.iot.discovery.element.Tag;
import org.jivesoftware.smackx.iot.element.NodeInfo;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;

public final class IoTDiscoveryManager extends Manager {

    private static Map<XMPPConnection, IoTDiscoveryManager> INSTANCES = new WeakHashMap<>();

    public static synchronized IoTDiscoveryManager getInstanceFor(XMPPConnection connection) {
        IoTDiscoveryManager manager = INSTANCES.get(connection);
        if (manager == null) {
            manager = new IoTDiscoveryManager(connection);
            INSTANCES.put(connection, manager);
        }
        return manager;
    }

    private Jid preconfiguredRegistry;

    private IoTDiscoveryManager(XMPPConnection connection) {
        super(connection);
    }

    /**
     * Try to find an XMPP IoT registry.
     *
     * @return the JID of a Thing Registry if one could be found.
     * @throws InterruptedException
     * @throws NotConnectedException
     * @throws XMPPErrorException
     * @throws NoResponseException
     * @see <a href="http://xmpp.org/extensions/xep-0347.html#findingregistry">XEP-0347 § 3.5 Finding Thing Registry</a>
     */
    public Jid findRegistry()
                    throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        if (preconfiguredRegistry != null) {
            return preconfiguredRegistry;
        }

        final XMPPConnection connection = connection();
        ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(connection);
        List<DiscoverInfo> discoverInfos = sdm.findServicesDiscoverInfo(Constants.IOT_DISCOVERY_NAMESPACE, true, true);
        if (!discoverInfos.isEmpty()) {
            return discoverInfos.get(0).getFrom();
        }

        return null;
    }

    // Thing Registration - XEP-0347 § 3.6 - 3.8

    public void registerThing(Thing thing)
                    throws NotConnectedException, InterruptedException, NoResponseException, XMPPErrorException {
        Jid registry = findRegistry();
        registerThing(registry, thing);
    }

    public void registerThing(Jid registry, Thing thing)
                    throws NotConnectedException, InterruptedException, NoResponseException, XMPPErrorException {
        IoTRegister iotRegister = new IoTRegister(thing.getMetaTags(), thing.getNodeInfo(), thing.isSelfOwened());
        iotRegister.setTo(registry);
        IQ result = connection().createPacketCollectorAndSend(iotRegister).nextResultOrThrow();
        if (result instanceof IoTClaimed) {
            // TODO handle <claimed/> in extra IoTException.
        }
        // TODO the thing should now be prepared to receive <removed/> or <disowned/> IQs from the registry
    }

    // Thing Claiming - XEP-0347 § 3.9

    public IoTClaimed claimThing(List<Tag> metaTags, boolean publicThing) throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        Jid registry = findRegistry();
        return claimThing(registry, metaTags, publicThing);
    }

    public IoTClaimed claimThing(Jid registry, List<Tag> metaTags, boolean publicThing) throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        IoTMine iotMine = new IoTMine(metaTags, publicThing);
        iotMine.setTo(registry);
        IoTClaimed iotClaimed = connection().createPacketCollectorAndSend(iotMine).nextResultOrThrow();
        return iotClaimed;
    }

    // Thing Removal - XEP-0347 § 3.10

    public void removeThing(BareJid thing)
                    throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        removeThing(thing, null);
    }

    public void removeThing(BareJid thing, NodeInfo nodeInfo)
                    throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        Jid registry = findRegistry();
        removeThing(registry, thing, nodeInfo);
    }

    public void removeThing(Jid registry, BareJid thing, NodeInfo nodeInfo)
                    throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        IoTRemove iotRemove = new IoTRemove(thing, nodeInfo);
        iotRemove.setTo(registry);
        connection().createPacketCollectorAndSend(iotRemove).nextResultOrThrow();
    }

    // Thing Disowning - XEP-0347 § 3.17

    public void disownThing(Jid thing)
                    throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        disownThing(thing, null);
    }

    public void disownThing(Jid thing, NodeInfo nodeInfo)
                    throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        Jid registry = findRegistry();
        disownThing(registry, thing, nodeInfo);
    }

    public void disownThing(Jid registry, Jid thing, NodeInfo nodeInfo)
                    throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        IoTDisown iotDisown = new IoTDisown(thing, nodeInfo);
        iotDisown.setTo(registry);
        connection().createPacketCollectorAndSend(iotDisown).nextResultOrThrow();
    }
}
