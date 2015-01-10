/**
 *
 * Copyright © 2015 Florian Schmaus
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
package org.jivesoftware.smackx.mam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.Manager;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPConnectionRegistry;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.forward.packet.Forwarded;
import org.jivesoftware.smackx.mam.filter.MamMessageFinFilter;
import org.jivesoftware.smackx.mam.filter.MamMessageResultFilter;
import org.jivesoftware.smackx.mam.packet.MamPacket;
import org.jivesoftware.smackx.mam.packet.MamPacket.MamFinExtension;
import org.jivesoftware.smackx.mam.packet.MamQueryIQ;
import org.jivesoftware.smackx.mam.packet.MamPacket.MamResultExtension;


/**
 * 
 * @see <a href="http://xmpp.org/extensions/xep-0313.html">XEP-0313: Message Archive Management</a>
 *
 */
public class MamManager extends Manager {

    static {
        XMPPConnectionRegistry.addConnectionCreationListener(new ConnectionCreationListener() {
            @Override
            public void connectionCreated(XMPPConnection connection) {
                getInstanceFor(connection);
            }
        });
    }
    
    private static final Map<XMPPConnection, MamManager> INSTANCES = new WeakHashMap<>();

    public static synchronized MamManager getInstanceFor(XMPPConnection connection) {
        MamManager mamManager = INSTANCES.get(connection);
        if (mamManager == null) {
            mamManager = new MamManager(connection);
            INSTANCES.put(connection, mamManager);
        }
        return mamManager;
    }

    private MamManager(XMPPConnection connection) {
        super(connection);
        ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(connection);
        sdm.addFeature(MamPacket.NAMESPACE);
    }

    private MamQueryResult queryArchive(MamQueryIQ mamQueryIq, long extraTimeout) throws NoResponseException,
                    XMPPErrorException, NotConnectedException {
        if (extraTimeout < 0) {
            throw new IllegalArgumentException("extra timeout must be zero or positive");
        }
        final XMPPConnection connection = connection();
        MamFinExtension mamFinExtension;
        PacketCollector resultCollector = connection.createPacketCollector(new MamMessageResultFilter(mamQueryIq));
        PacketCollector finMessageCollector = connection.createPacketCollector(new MamMessageFinFilter(mamQueryIq));
        try {
            connection.createPacketCollectorAndSend(mamQueryIq).nextResultOrThrow();
            Message mamFinMessage = finMessageCollector.nextResultOrThrow(connection.getPacketReplyTimeout() + extraTimeout);
            mamFinExtension = MamFinExtension.from(mamFinMessage);
        }
        finally {
            resultCollector.cancel();
            finMessageCollector.cancel();
        }
        List<Forwarded> messages = new ArrayList<>(resultCollector.getCollectedNumber());
        for (Message resultMessage = resultCollector.pollResult(); resultMessage != null;) {
            // XEP-313 § 4.2
            MamResultExtension mamResultExtension = MamResultExtension.from(resultMessage);
            messages.add(mamResultExtension.getForwarded());
        }
        return new MamQueryResult(messages, mamFinExtension);
    }

    public static class MamQueryResult {
        public final List<Forwarded> messages;
        public final MamFinExtension mamFin;

        private MamQueryResult(List<Forwarded> messages, MamFinExtension mamFin) {
            this.messages = messages;
            this.mamFin = mamFin;
        }
    }
}
