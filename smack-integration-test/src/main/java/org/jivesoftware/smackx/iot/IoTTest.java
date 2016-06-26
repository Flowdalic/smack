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

import java.util.Collections;

import org.igniterealtime.smack.inttest.AbstractSmackIntegrationTest;
import org.igniterealtime.smack.inttest.SmackIntegrationTest;
import org.igniterealtime.smack.inttest.SmackIntegrationTestEnvironment;
import org.igniterealtime.smack.inttest.TestNotPossibleException;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.iot.data.ThingMomentaryReadOutRequest;
import org.jivesoftware.smackx.iot.data.ThingMomentaryReadOutResult;
import org.jivesoftware.smackx.iot.data.element.IoTDataField;
import org.jivesoftware.smackx.iot.data.element.IoTDataField.IntField;
import org.jivesoftware.smackx.iot.discovery.IoTDiscoveryManager;

public class IoTTest extends AbstractSmackIntegrationTest {

    private final IoTDiscoveryManager iotDiscoveryManagerOne;

    private final IoTDiscoveryManager iotDiscoveryManagerTwo;

    public IoTTest(SmackIntegrationTestEnvironment environment) throws NoResponseException, XMPPErrorException, NotConnectedException, InterruptedException, TestNotPossibleException {
        super(environment);
        IoTDiscoveryIntegrationTest.checkPrerequisites(connection);
        iotDiscoveryManagerOne = IoTDiscoveryManager.getInstanceFor(conOne);
        iotDiscoveryManagerTwo = IoTDiscoveryManager.getInstanceFor(conTwo);
    }

    /**
     * Roles:
     * - conOne: Owner of data thing.
     * - conTwo: The data thing.
     * - conThree: The thing that wants to read data from data thing.
     * @throws SmackException 
     * @throws InterruptedException 
     * @throws XMPPErrorException 
     */
    @SmackIntegrationTest
    public void threeEntityDataReadOutTest() throws XMPPErrorException, InterruptedException, SmackException {
        final String key = StringUtils.randomString(12);
        final String sn = StringUtils.randomString(12);
        Thing dataThing = Thing.builder().setKey(key).setSerialNumber(sn).setMomentaryReadOutRequestHandler(new ThingMomentaryReadOutRequest() {
            @Override
            public void momentaryReadOutRequest(ThingMomentaryReadOutResult callback) {
                IoTDataField.IntField field = new IntField("timestamp", (int) (System.currentTimeMillis() / 1000));
                callback.momentaryReadOut(Collections.singletonList(field));
            }
        }).build();
        IoTDiscoveryIntegrationTest.registerThing(iotDiscoveryManagerTwo, dataThing);

        iotDiscoveryManagerOne.claimThing(dataThing.getMetaTags());
    }
}
