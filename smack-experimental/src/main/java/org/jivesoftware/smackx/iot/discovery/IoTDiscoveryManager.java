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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.Manager;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPConnectionRegistry;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.iqrequest.AbstractIqRequestHandler;
import org.jivesoftware.smack.iqrequest.IQRequestHandler.Mode;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.iot.Thing;
import org.jivesoftware.smackx.iot.discovery.element.Constants;
import org.jivesoftware.smackx.iot.discovery.element.IoTClaimed;
import org.jivesoftware.smackx.iot.discovery.element.IoTDisown;
import org.jivesoftware.smackx.iot.discovery.element.IoTDisowned;
import org.jivesoftware.smackx.iot.discovery.element.IoTMine;
import org.jivesoftware.smackx.iot.discovery.element.IoTRegister;
import org.jivesoftware.smackx.iot.discovery.element.IoTRemove;
import org.jivesoftware.smackx.iot.discovery.element.IoTUnregister;
import org.jivesoftware.smackx.iot.discovery.element.Tag;
import org.jivesoftware.smackx.iot.element.NodeInfo;
import org.jivesoftware.smackx.iot.provisioning.IoTProvisioningManager;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;

/**
 * A manager for XMPP IoT Discovery. Used to register and discover things.
 *
 * @author Florian Schmaus {@literal <flo@geekplace.eu>}
 * @see <a href="http://xmpp.org/extensions/xep-0347.html">XEP-0347: Internet of Things - Discovery</a>
 *
 */
public final class IoTDiscoveryManager extends Manager {

    private static final Logger LOGGER = Logger.getLogger(IoTDiscoveryManager.class.getName());

    private static final Map<XMPPConnection, IoTDiscoveryManager> INSTANCES = new WeakHashMap<>();

    // Ensure a IoTProvisioningManager exists for every connection.
    static {
        XMPPConnectionRegistry.addConnectionCreationListener(new ConnectionCreationListener() {
            public void connectionCreated(XMPPConnection connection) {
                getInstanceFor(connection);
            }
        });
    }

    public static synchronized IoTDiscoveryManager getInstanceFor(XMPPConnection connection) {
        IoTDiscoveryManager manager = INSTANCES.get(connection);
        if (manager == null) {
            manager = new IoTDiscoveryManager(connection);
            INSTANCES.put(connection, manager);
        }
        return manager;
    }

    private Jid preconfiguredRegistry;

    private final Set<Jid> usedRegistries = new HashSet<>();

    /**
     * Internal state of the things. Uses <code>null</code> for the single thing without node info attached.
     */
    private final Map<NodeInfo, ThingState> things = new HashMap<>();

    private IoTDiscoveryManager(XMPPConnection connection) {
        super(connection);

        connection.registerIQRequestHandler(
                        new AbstractIqRequestHandler(IoTClaimed.ELEMENT, IoTClaimed.NAMESPACE, IQ.Type.set, Mode.sync) {
                            @Override
                            public IQ handleIQRequest(IQ iqRequest) {
                                IoTClaimed iotClaimed = (IoTClaimed) iqRequest;
                                Jid owner = iotClaimed.getJid();
                                NodeInfo nodeInfo = iotClaimed.getNodeInfo();
                                // Update the state.
                                ThingState state = getStateFor(nodeInfo);
                                state.owner = owner.asBareJid();
                                LOGGER.info("Our thing got claimed by " + owner + ". " + iotClaimed);

                                IoTProvisioningManager iotProvisioningManager = IoTProvisioningManager.getInstanceFor(
                                                connection());
                                try {
                                    iotProvisioningManager.sendFriendshipRequest(owner);
                                }
                                catch (NotConnectedException | InterruptedException e) {
                                    LOGGER.log(Level.WARNING, "Could not friendship owner", e);
                                }

                                return IQ.createResultIQ(iqRequest);
                            }
                        });

        connection.registerIQRequestHandler(new AbstractIqRequestHandler(IoTDisowned.ELEMENT, IoTDisowned.NAMESPACE,
                        IQ.Type.set, Mode.sync) {
            @Override
            public IQ handleIQRequest(IQ iqRequest) {
                IoTDisowned iotDisowned = (IoTDisowned) iqRequest;
                Jid from = iqRequest.getFrom();

                NodeInfo nodeInfo = iotDisowned.getNodeInfo();
                ThingState state = getStateFor(nodeInfo);
                if (!from.equals(state.registry)) {
                    LOGGER.severe("Received <disowned/> for " + nodeInfo + " from " + from
                                    + " but this is not the registry " + state.registry + " of the thing.");
                    return null;
                }

                if (state.owner != null) {
                    state.owner = null;
                } else {
                    LOGGER.fine("Received <disowned/> for " + nodeInfo + " but thing was not owned.");
                }

                return IQ.createResultIQ(iqRequest);
            }

        });
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
                    throws NotConnectedException, InterruptedException, NoResponseException, XMPPErrorException, IoTClaimedException {
        Jid registry = findRegistry();
        registerThing(registry, thing);
    }

    public void registerThing(Jid registry, Thing thing)
                    throws NotConnectedException, InterruptedException, NoResponseException, XMPPErrorException, IoTClaimedException {
        IoTRegister iotRegister = new IoTRegister(thing.getMetaTags(), thing.getNodeInfo(), thing.isSelfOwened());
        iotRegister.setTo(registry);
        IQ result = connection().createPacketCollectorAndSend(iotRegister).nextResultOrThrow();
        if (result instanceof IoTClaimed) {
            IoTClaimed iotClaimedResult = (IoTClaimed) result;
            throw new IoTClaimedException(iotClaimedResult);
        }

        ThingState state = getStateFor(thing.getNodeInfo());
        state.registry = registry.asBareJid();

        interactWithRegistry(registry);

        // TODO the thing should now be prepared to receive <removed/> or <disowned/> IQs from the registry
    }

    // Thing Claiming - XEP-0347 § 3.9

    public IoTClaimed claimThing(Collection<Tag> metaTags) throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        return claimThing(metaTags, true);
    }

    public IoTClaimed claimThing(Collection<Tag> metaTags, boolean publicThing) throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        Jid registry = findRegistry();
        return claimThing(registry, metaTags, publicThing);
    }

    public IoTClaimed claimThing(Jid registry, Collection<Tag> metaTags, boolean publicThing) throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        interactWithRegistry(registry);

        IoTMine iotMine = new IoTMine(metaTags, publicThing);
        iotMine.setTo(registry);
        IoTClaimed iotClaimed = connection().createPacketCollectorAndSend(iotMine).nextResultOrThrow();

        // The 'jid' attribute of the <claimed/> response now represents the XMPP address of the thing we just successfully claimed.
        Jid thing = iotClaimed.getJid();

        IoTProvisioningManager.getInstanceFor(connection()).sendFriendshipRequest(thing);

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
        interactWithRegistry(registry);

        IoTRemove iotRemove = new IoTRemove(thing, nodeInfo);
        iotRemove.setTo(registry);
        connection().createPacketCollectorAndSend(iotRemove).nextResultOrThrow();
    }

    // Thing Unregistering - XEP-0347 § 3.16

    public void unregister()
                    throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        unregister(null);
    }

    public void unregister(NodeInfo nodeInfo)
                    throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        Jid registry = findRegistry();
        unregister(registry, nodeInfo);
    }

    public void unregister(Jid registry, NodeInfo nodeInfo)
                    throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        interactWithRegistry(registry);

        IoTUnregister iotUnregister = new IoTUnregister(nodeInfo);
        iotUnregister.setTo(registry);
        connection().createPacketCollectorAndSend(iotUnregister).nextResultOrThrow();
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
        interactWithRegistry(registry);

        IoTDisown iotDisown = new IoTDisown(thing, nodeInfo);
        iotDisown.setTo(registry);
        connection().createPacketCollectorAndSend(iotDisown).nextResultOrThrow();
    }

    // Registry utility methods

    public boolean isRegistry(BareJid jid) throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        // At some point 'usedRegistries' will also contain the registry returned by findRegistry(), but since this is
        // not the case from the beginning, we perform findRegistry().equals(jid) too.
        if (findRegistry().equals(jid)) {
            return true;
        }
        if (usedRegistries.contains(jid)) {
            return true;
        }
        return false;
    }

    private void interactWithRegistry(Jid registry) throws NotConnectedException, InterruptedException {
        boolean isNew = usedRegistries.add(registry);
        if (!isNew) {
            return;
        }
        IoTProvisioningManager iotProvisioningManager = IoTProvisioningManager.getInstanceFor(connection());
        iotProvisioningManager.sendFriendshipRequestIfRequired(registry);
    }

    private ThingState getStateFor(NodeInfo nodeInfo) {
        ThingState state = things.get(nodeInfo);
        if (state == null) {
            state = new ThingState();
            things.put(nodeInfo, state);
        }
        return state;
    }

    private static class ThingState {
        private BareJid registry;
        private BareJid owner;
    }
}
