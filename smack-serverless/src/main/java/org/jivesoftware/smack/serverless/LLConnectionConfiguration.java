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

import org.jivesoftware.smack.ConnectionConfiguration;

import javax.net.SocketFactory;
import java.net.Socket;

public class LLConnectionConfiguration extends ConnectionConfiguration {

    private LLPresence remotePresence;
    private LLPresence localPresence;
    private Socket socket;

    /**
     * Holds the socket factory that is used to generate the socket in the connection.
     */
    private SocketFactory socketFactory;

    /**
     * Initiating a Link Local Connection when the client is acting has a Host.
     * @param localPresence Local Link Presence
     * @param remoteSocket The socket that is associated with this local connection
     */
    public LLConnectionConfiguration(LLPresence localPresence, Socket remoteSocket) {
        super(null);
        this.localPresence = localPresence;
        this.socket = remoteSocket;
    }

    /**
     * This tells if this is a host connection or not.
     * @return true if this is a host connection and false otherwise
     */
    public boolean isInitiator() {
        return socket == null;
    }

    /**
     * Return the remote client's link-local presence information.
     * @return the remote client's link-local presence information
     */
    public LLPresence getRemotePresence() {
        return remotePresence;
    }

    /**
     * Return this client's link-local presence information.
     * @return this client's link-local presence information
     */
    public LLPresence getLocalPresence() {
        return localPresence;
    }

    /**
     * Return the socket which was instantiated when a connection to a client
     * was established.
     * @return the socket established when the remote client connected
     */
    public Socket getSocket() {
        return socket;
    }

}
