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
package org.jivesoftware.smackx.iot;

import org.igniterealtime.smack.inttest.AbstractSmackIntegrationTest;
import org.igniterealtime.smack.inttest.SmackIntegrationTest;
import org.igniterealtime.smack.inttest.SmackIntegrationTestEnvironment;
import org.igniterealtime.smack.inttest.TestNotPossibleException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.iot.discovery.IoTDiscoveryManager;
import org.jxmpp.jid.Jid;

public class IoTDiscoveryIntegrationTest extends AbstractSmackIntegrationTest {

    private final IoTDiscoveryManager discoveryManagerOne;

    public IoTDiscoveryIntegrationTest(SmackIntegrationTestEnvironment environment) throws NoResponseException,
                    XMPPErrorException, NotConnectedException, InterruptedException, TestNotPossibleException {
        super(environment);
        discoveryManagerOne = IoTDiscoveryManager.getInstanceFor(conOne);
        Jid registry = discoveryManagerOne.findRegistry();
        if (registry == null) {
            throw new TestNotPossibleException("Could not find IoT Registry");
        }
    }

    @SmackIntegrationTest
    public void printRegistry() throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException {
        Jid registry = discoveryManagerOne.findRegistry();
        LOGGER.info("IoT Registry: " + registry);
    }
}
