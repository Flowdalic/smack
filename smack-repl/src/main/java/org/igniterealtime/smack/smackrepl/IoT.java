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
package org.igniterealtime.smack.smackrepl;

import org.jivesoftware.smack.XMPPException;

import java.io.IOException;
import java.util.Collections;

import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.iot.Thing;
import org.jivesoftware.smackx.iot.data.ThingMomentaryReadOutRequest;
import org.jivesoftware.smackx.iot.data.ThingMomentaryReadOutResult;
import org.jivesoftware.smackx.iot.data.element.IoTDataField;
import org.jivesoftware.smackx.iot.data.element.IoTDataField.IntField;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.impl.JidCreate;

public class IoT {

    public static void actAsDataThing(String jidString, String password) throws XMPPException, SmackException, IOException, InterruptedException {
        BareJid jid = JidCreate.bareFrom(jidString);
        XMPPTCPConnectionConfiguration configuration = XMPPTCPConnectionConfiguration.builder().setUsernameAndPassword(
                        jid, password).setSecurityMode(SecurityMode.disabled).build();
        XMPPTCPConnection connection = new XMPPTCPConnection(configuration);
        try {
            actAsDataThing(connection);
        } finally {
            connection.disconnect();
        }
    }

    private static void actAsDataThing(XMPPTCPConnection connection) throws XMPPException, SmackException, IOException, InterruptedException {
        connection.connect().login();
        final String key = StringUtils.randomString(12);
        final String sn = StringUtils.randomString(12);
        Thing dataThing = Thing.builder().setKey(key).setSerialNumber(sn).setMomentaryReadOutRequestHandler(new ThingMomentaryReadOutRequest() {
            @Override
            public void momentaryReadOutRequest(ThingMomentaryReadOutResult callback) {
                IoTDataField.IntField field = new IntField("timestamp", (int) (System.currentTimeMillis() / 1000));
                callback.momentaryReadOut(Collections.singletonList(field));
            }
        }).build();
    }
}
