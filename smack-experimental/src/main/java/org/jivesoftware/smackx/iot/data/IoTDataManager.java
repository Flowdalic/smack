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
package org.jivesoftware.smackx.iot.data;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.Manager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPConnectionRegistry;
import org.jivesoftware.smack.iqrequest.AbstractIqRequestHandler;
import org.jivesoftware.smack.iqrequest.IQRequestHandler.Mode;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smackx.iot.data.element.IoTDataField;
import org.jivesoftware.smackx.iot.data.element.IoTDataRequest;

public final class IoTDataManager extends Manager {

    private static final Map<XMPPConnection, IoTDataManager> INSTANCES = new WeakHashMap<>();

    // Ensure a IoTDataManager exists for every connection.
    static {
        XMPPConnectionRegistry.addConnectionCreationListener(new ConnectionCreationListener() {
            public void connectionCreated(XMPPConnection connection) {
                getInstanceFor(connection);
            }
        });
    }

    public static synchronized IoTDataManager getInstanceFor(XMPPConnection connection) {
        IoTDataManager manager = INSTANCES.get(connection);
        if (manager == null) {
            manager = new IoTDataManager(connection);
            INSTANCES.put(connection, manager);
        }
        return manager;
    }

    private IoTDataManager(XMPPConnection connection) {
        super(connection);

        connection.registerIQRequestHandler(new AbstractIqRequestHandler(IoTDataRequest.ELEMENT,
                        IoTDataRequest.NAMESPACE, IQ.Type.get, Mode.async) {
            @Override
            public IQ handleIQRequest(IQ iqRequest) {
                // TODO verify that iqRequest.from is friend.
                // TODO return error if not at least one thing registered.
                IoTDataRequest dataRequest = (IoTDataRequest) iqRequest;

                if (!dataRequest.isMomentary()) {
                    // TODO return error IQ that non momentary requests are not implemented yet.
                    return null;
                }

                // Callback hell begins here. :) XEP-0323 decouples the read-out results from the IQ result. I'm not
                // sure if I would have made the same design decision but the reasons where likely being able to get a
                // fast read-out acknowledgement back to the requester even with sensors that take "a long time" to
                // read-out their values. I had designed that as special case and made the "results in IQ response" the
                // normal case.
                ThingMomentaryReadOutRequest readOutRequest = null;
                readOutRequest.momentaryReadOutRequest(new ThingMomentaryReadOutResult() {
                    @Override
                    public void momentaryReadOut(List<IoTDataField> results) {
                        // TODO return results in message.
                    }
                });
                // TODO return result IQ
                return null;
            }
        });
    }

}
