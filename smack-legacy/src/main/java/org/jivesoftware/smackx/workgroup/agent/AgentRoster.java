/**
 *
 * Copyright 2003-2007 Jive Software.
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

package org.jivesoftware.smackx.workgroup.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.StanzaBuilder;

import org.jivesoftware.smackx.workgroup.packet.AgentStatus;
import org.jivesoftware.smackx.workgroup.packet.AgentStatusRequest;

import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

/**
 * Manges information about the agents in a workgroup and their presence.
 *
 * @author Matt Tucker
 * @see AgentSession#getAgentRoster()
 */
public class AgentRoster {
    private static final Logger LOGGER = Logger.getLogger(AgentRoster.class.getName());
    private static final int EVENT_AGENT_ADDED = 0;
    private static final int EVENT_AGENT_REMOVED = 1;
    private static final int EVENT_PRESENCE_CHANGED = 2;

    private final XMPPConnection connection;
    private final EntityBareJid workgroupJID;
    private final List<EntityBareJid> entries = new ArrayList<>();
    private final List<AgentRosterListener> listeners = new ArrayList<>();
    private final Map<Jid, Map<Resourcepart, Presence>> presenceMap = new HashMap<>();
    // The roster is marked as initialized when at least a single roster packet
    // has been received and processed.
    boolean rosterInitialized = false;

    /**
     * Constructs a new AgentRoster.
     *
     * @param connection an XMPP connection.
     * @throws NotConnectedException if the XMPP connection is not connected.
     * @throws InterruptedException if the calling thread was interrupted.
     */
    AgentRoster(XMPPConnection connection, EntityBareJid workgroupJID) throws NotConnectedException, InterruptedException {
        this.connection = connection;
        this.workgroupJID = workgroupJID;
        // Listen for any roster packets.
        StanzaFilter rosterFilter = new StanzaTypeFilter(AgentStatusRequest.class);
        connection.addAsyncStanzaListener(new AgentStatusListener(), rosterFilter);
        // Listen for any presence packets.
        connection.addAsyncStanzaListener(new PresencePacketListener(),
                new StanzaTypeFilter(Presence.class));

        // Send request for roster.
        AgentStatusRequest request = new AgentStatusRequest();
        request.setTo(workgroupJID);
        connection.sendStanza(request);
    }

    /**
     * Reloads the entire roster from the server. This is an asynchronous operation,
     * which means the method will return immediately, and the roster will be
     * reloaded at a later point when the server responds to the reload request.
     * @throws NotConnectedException if the XMPP connection is not connected.
     * @throws InterruptedException if the calling thread was interrupted.
     */
    public void reload() throws NotConnectedException, InterruptedException {
        AgentStatusRequest request = new AgentStatusRequest();
        request.setTo(workgroupJID);
        connection.sendStanza(request);
    }

    /**
     * Adds a listener to this roster. The listener will be fired anytime one or more
     * changes to the roster are pushed from the server.
     *
     * @param listener an agent roster listener.
     */
    public void addListener(AgentRosterListener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);

                // Fire events for the existing entries and presences in the roster
                for (EntityBareJid jid : getAgents()) {
                    // Check again in case the agent is no longer in the roster (highly unlikely
                    // but possible)
                    if (entries.contains(jid)) {
                        // Fire the agent added event
                        listener.agentAdded(jid);
                        Jid j;
                        try {
                            j = JidCreate.from(jid);
                        }
                        catch (XmppStringprepException e) {
                            throw new IllegalStateException(e);
                        }
                        Map<Resourcepart, Presence> userPresences = presenceMap.get(j);
                        if (userPresences != null) {
                            Iterator<Presence> presences = userPresences.values().iterator();
                            while (presences.hasNext()) {
                                // Fire the presence changed event
                                listener.presenceChanged(presences.next());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes a listener from this roster. The listener will be fired anytime one or more
     * changes to the roster are pushed from the server.
     *
     * @param listener a roster listener.
     */
    public void removeListener(AgentRosterListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Returns a count of all agents in the workgroup.
     *
     * @return the number of agents in the workgroup.
     */
    public int getAgentCount() {
        return entries.size();
    }

    /**
     * Returns all agents (String JID values) in the workgroup.
     *
     * @return all entries in the roster.
     */
    public Set<EntityBareJid> getAgents() {
        Set<EntityBareJid> agents = new HashSet<>();
        synchronized (entries) {
            for (Iterator<EntityBareJid> i = entries.iterator(); i.hasNext();) {
                agents.add(i.next());
            }
        }
        return Collections.unmodifiableSet(agents);
    }

    /**
     * Returns true if the specified XMPP address is an agent in the workgroup.
     *
     * @param jid the XMPP address of the agent (e.g."jsmith@example.com"). The
     *            address can be in any valid format (e.g. "domain/resource", "user@domain"
     *            or "user@domain/resource").
     * @return true if the XMPP address is an agent in the workgroup.
     */
    public boolean contains(Jid jid) {
        if (jid == null) {
            return false;
        }
        synchronized (entries) {
            for (Iterator<EntityBareJid> i = entries.iterator(); i.hasNext();) {
                EntityBareJid entry = i.next();
                if (entry.equals(jid)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the presence info for a particular agent, or <code>null</code> if the agent
     * is unavailable (offline) or if no presence information is available.
     *
     * @param user a fully qualified xmpp JID. The address could be in any valid format (e.g.
     *             "domain/resource", "user@domain" or "user@domain/resource").
     * @return the agent's current presence, or <code>null</code> if the agent is unavailable
     *         or if no presence information is available.
     */
    public Presence getPresence(Jid user) {
        Jid key = getPresenceMapKey(user);
        Map<Resourcepart, Presence> userPresences = presenceMap.get(key);
        if (userPresences == null) {
            Presence presence = StanzaBuilder.buildPresence()
                    .ofType(Presence.Type.unavailable)
                    .from(user)
                    .build();
            return presence;
        }
        else {
            // Find the resource with the highest priority
            // Might be changed to use the resource with the highest availability instead.
            Iterator<Resourcepart> it = userPresences.keySet().iterator();
            Presence p;
            Presence presence = null;

            while (it.hasNext()) {
                p = userPresences.get(it.next());
                if (presence == null) {
                    presence = p;
                }
                else {
                    if (p.getPriority() > presence.getPriority()) {
                        presence = p;
                    }
                }
            }
            if (presence == null) {
                presence = synthesizeUnvailablePresence(user);
                return presence;
            }
            else {
                return presence;
            }
        }
    }

    /**
     * Returns the key to use in the presenceMap for a fully qualified xmpp ID. The roster
     * can contain any valid address format such us "domain/resource", "user@domain" or
     * "user@domain/resource". If the roster contains an entry associated with the fully qualified
     * xmpp ID then use the fully qualified xmpp ID as the key in presenceMap, otherwise use the
     * bare address. Note: When the key in presenceMap is a fully qualified xmpp ID, the
     * userPresences is useless since it will always contain one entry for the user.
     *
     * @param user the fully qualified xmpp ID, e.g. jdoe@example.com/Work.
     * @return the key to use in the presenceMap for the fully qualified xmpp ID.
     */
    private Jid getPresenceMapKey(Jid user) {
        Jid key = user;
        if (!contains(user)) {
            key = user.asEntityBareJidIfPossible();
        }
        return key;
    }

    /**
     * Fires event to listeners.
     */
    private void fireEvent(int eventType, Object eventObject) {
        AgentRosterListener[] listeners;
        synchronized (this.listeners) {
            listeners = new AgentRosterListener[this.listeners.size()];
            this.listeners.toArray(listeners);
        }
        for (int i = 0; i < listeners.length; i++) {
            switch (eventType) {
                case EVENT_AGENT_ADDED:
                    listeners[i].agentAdded((EntityBareJid) eventObject);
                    break;
                case EVENT_AGENT_REMOVED:
                    listeners[i].agentRemoved((EntityBareJid) eventObject);
                    break;
                case EVENT_PRESENCE_CHANGED:
                    listeners[i].presenceChanged((Presence) eventObject);
                    break;
            }
        }
    }

    private static Presence synthesizeUnvailablePresence(Jid from) {
        return StanzaBuilder.buildPresence()
                .ofType(Presence.Type.unavailable)
                .from(from)
                .build();
    }

    /**
     * Listens for all presence packets and processes them.
     */
    private final class PresencePacketListener implements StanzaListener {
        @Override
        public void processStanza(Stanza packet) {
            Presence presence = (Presence) packet;
            EntityFullJid from = presence.getFrom().asEntityFullJidIfPossible();
            if (from == null) {
                // TODO Check if we need to ignore these presences or this is a server bug?
                LOGGER.warning("Presence with non full JID from: " + presence.toXML());
                return;
            }
            Jid key = getPresenceMapKey(from);

            // If an "available" packet, add it to the presence map. Each presence map will hold
            // for a particular user a map with the presence packets saved for each resource.
            if (presence.getType() == Presence.Type.available) {
                // Ignore the presence packet unless it has an agent status extension.
                AgentStatus agentStatus = (AgentStatus) presence.getExtensionElement(
                        AgentStatus.ELEMENT_NAME, AgentStatus.NAMESPACE);
                if (agentStatus == null) {
                    return;
                }
                // Ensure that this presence is coming from an Agent of the same workgroup
                // of this Agent
                else if (!workgroupJID.equals(agentStatus.getWorkgroupJID())) {
                    return;
                }
                Map<Resourcepart, Presence> userPresences;
                // Get the user presence map
                if (presenceMap.get(key) == null) {
                    userPresences = new HashMap<>();
                    presenceMap.put(key, userPresences);
                }
                else {
                    userPresences = presenceMap.get(key);
                }
                // Add the new presence, using the resources as a key.
                synchronized (userPresences) {
                    userPresences.put(from.getResourcepart(), presence);
                }
                // Fire an event.
                synchronized (entries) {
                    for (Iterator<EntityBareJid> i = entries.iterator(); i.hasNext();) {
                        EntityBareJid entry = i.next();
                        if (entry.equals(key.asEntityBareJidIfPossible())) {
                            fireEvent(EVENT_PRESENCE_CHANGED, packet);
                        }
                    }
                }
            }
            // If an "unavailable" packet, remove any entries in the presence map.
            else if (presence.getType() == Presence.Type.unavailable) {
                if (presenceMap.get(key) != null) {
                    Map<Resourcepart, Presence> userPresences = presenceMap.get(key);
                    synchronized (userPresences) {
                        userPresences.remove(from.getResourcepart());
                    }
                    if (userPresences.isEmpty()) {
                        presenceMap.remove(key);
                    }
                }
                // Fire an event.
                synchronized (entries) {
                    for (EntityBareJid entry : entries) {
                        if (entry.equals(key.asEntityBareJidIfPossible())) {
                            fireEvent(EVENT_PRESENCE_CHANGED, packet);
                        }
                    }
                }
            }
        }
    }

    /**
     * Listens for all roster packets and processes them.
     */
    private final class AgentStatusListener implements StanzaListener {

        @Override
        public void processStanza(Stanza packet) {
            if (packet instanceof AgentStatusRequest) {
                AgentStatusRequest statusRequest = (AgentStatusRequest) packet;
                for (Iterator<AgentStatusRequest.Item> i = statusRequest.getAgents().iterator(); i.hasNext();) {
                    AgentStatusRequest.Item item = i.next();
                    EntityBareJid agentJID = item.getJID();
                    if ("remove".equals(item.getType())) {

                        // Removing the user from the roster, so remove any presence information
                        // about them.
                        presenceMap.remove(agentJID.asBareJid());
                        // Fire event for roster listeners.
                        fireEvent(EVENT_AGENT_REMOVED, agentJID);
                    }
                    else {
                        entries.add(agentJID);
                        // Fire event for roster listeners.
                        fireEvent(EVENT_AGENT_ADDED, agentJID);
                    }
                }

                // Mark the roster as initialized.
                rosterInitialized = true;
            }
        }
    }
}
