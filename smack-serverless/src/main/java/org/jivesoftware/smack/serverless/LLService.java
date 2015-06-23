/**
 *
 * Copyright 2015 Ishan Khanna.
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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;
import org.jxmpp.jid.BareJid;

public abstract class LLService {

    private static LLService service = null;

    static final int DEFAULT_MIN_PORT = 2300;
    static final int DEFAULT_MAX_PORT = 2400;

    protected LLPresence presence;
    private boolean done = false;
    private boolean initiated = false;
    private Thread listenerThread;

    private ServerSocket socket;

    private Map<String,XMPPLLConnection> incoming =
            new ConcurrentHashMap<String,XMPPLLConnection>();
    private Map<String,XMPPLLConnection> outgoing =
            new ConcurrentHashMap<String,XMPPLLConnection>();

    static {
        SmackConfiguration.getVersion();
    }

    public static LLService getInstance() {

        if (service == null) {
            service = new JmDNSService();
        }

        return service;
    }

    /**
     * Fetch list of all the users who are present on the Link Local network.
     * 
     * @return List<BareJid> list of BareJid(s) of all available clients
     */
    public abstract List<BareJid> getAllClientsPresentOnLLNetwork();
    
    
    /**
     * Marks presence of a client on the Link Local Network. Marking presence
     * on the network does not mean client is available for chat. It might so
     * happen that client is present on the network but is Away, Busy or 
     * Unavailable for chat. 
     *
     * @return true if client was able to broadcast presence on the network
     * successfully and false otherwise
     */
    public abstract void announcePresence(LLPresence presence) throws XMPPException;

    
    /**
    * Marks absence of a client on the Link Local Network. Marking absence
    * on the network means client is no longer connected to the Link Local 
    * Network, in simpler terms client has logged out. 
    * 
    * @return true if client was able to broadcast absence on the network
    * successfully and false otherwise
    */
    public abstract void concealPresence();

    public void init() throws XMPPException{

        // allocate a new port for remote clients to connect to
        socket = bindRange(DEFAULT_MIN_PORT, DEFAULT_MAX_PORT);

        // register service on the allocated port
        //announcePresence();

        // start to listen for new connections
        listenerThread = new Thread() {
            public void run() {
                try {
                    // Listen for connections
                    listenForConnections();

                } catch (XMPPException e) {
                }
            }
        };
        listenerThread.setName("Smack Link-local Service Listener");
        listenerThread.setDaemon(true);
        listenerThread.start();

        initiated = true;

    }

    /**
     * Listen for new connections on socket, and spawn XMPPLLConnections
     * when new connections are established.
     *
     * @throws XMPPException whenever an exception occurs
     */
    private void listenForConnections() throws XMPPException {
        while (!done) {
            try {
                // wait for new connection
                Socket s = socket.accept();

                LLConnectionConfiguration config =
                        new LLConnectionConfiguration(presence, s);

            }
            catch (SocketException se) {
                // If we are closing down, it's probably closed socket exception.
                if (!done) {
                    throw new XMPPException.XMPPErrorException("Link-local service unexpectedly closed down.",
                            new XMPPError(XMPPError.Condition.undefined_condition), se);
                }
            }
            catch (IOException ioe) {
                throw new XMPPException.XMPPErrorException("Link-local service unexpectedly closed down.",
                        new XMPPError(XMPPError.Condition.undefined_condition), ioe);
            }
        }
    }


    /**
     * Returns a connection to a given service name.
     * First checks for an outgoing connection, if noone exists,
     * try ingoing.
     *
     * @param serviceName the service name
     * @return a connection associated with the service name or null if no
     * connection is available.
     */
    XMPPLLConnection getConnectionTo(String serviceName) {
        XMPPLLConnection connection = outgoing.get(serviceName);
        if (connection != null)
            return connection;
        return incoming.get(serviceName);
    }

    protected void serviceNameChanged(String newName, String oldName) {
        // update our own presence with the new name, for future connections
        presence.setServiceName(newName);

        // clean up connections
        XMPPLLConnection c;
        c = getConnectionTo(oldName);
        if (c != null)
            c.disconnect();
        c = getConnectionTo(newName);
        if (c != null)
            c.disconnect();

    }

    /**
     * Bind one socket to any port within a given range.
     *
     * @param min the minimum port number allowed
     * @param max hte maximum port number allowed
     * @throws XMPPException if binding failed on all allowed ports.
     */
    private static ServerSocket bindRange(int min, int max) throws XMPPException {
        // TODO this method exists also for the local socks5 proxy code and should be factored out into a util
        int port = 0;
        for (int try_port = min; try_port <= max; try_port++) {
            try {
                ServerSocket socket = new ServerSocket(try_port);
                return socket;
            }
            catch (IOException e) {
                // failed to bind, try next
            }
        }
        throw new XMPPException.XMPPErrorException("Unable to bind port, no ports available.",
                new XMPPError(XMPPError.Condition.resource_constraint));
    }
}
