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

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.igniterealtime.smack.inttest.AbstractSmackIntegrationTest;
import org.igniterealtime.smack.inttest.SmackIntegrationTest;
import org.igniterealtime.smack.inttest.SmackIntegrationTestEnvironment;
import org.jivesoftware.smack.roster.RosterIntegrationTest;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.iot.data.IoTDataManager;
import org.jivesoftware.smackx.iot.data.ThingMomentaryReadOutRequest;
import org.jivesoftware.smackx.iot.data.ThingMomentaryReadOutResult;
import org.jivesoftware.smackx.iot.data.element.IoTDataField;
import org.jivesoftware.smackx.iot.data.element.IoTDataField.IntField;
import org.jivesoftware.smackx.iot.data.element.IoTFieldsExtension;

public class IoTDataIntegrationTest extends AbstractSmackIntegrationTest {

    private final IoTDataManager iotDataManagerOne;

    private final IoTDataManager iotDataManagerTwo;

    public IoTDataIntegrationTest(SmackIntegrationTestEnvironment environment) {
        super(environment);
        iotDataManagerOne = IoTDataManager.getInstanceFor(conOne);
        iotDataManagerTwo = IoTDataManager.getInstanceFor(conTwo);
    }

    /**
     * Connection one provides a thing, which momentary value is read out by connection two.
     *
     * @throws Exception 
     * @throws TimeoutException 
     */
    @SmackIntegrationTest
    public void dataTest() throws TimeoutException, Exception {
        final String key = StringUtils.randomString(12);
        final String sn = StringUtils.randomString(12);
        final int value = INSECURE_RANDOM.nextInt();

        Thing dataThing = Thing.builder().setKey(key).setSerialNumber(sn).setMomentaryReadOutRequestHandler(new ThingMomentaryReadOutRequest() {
            @Override
            public void momentaryReadOutRequest(ThingMomentaryReadOutResult callback) {
                IoTDataField.IntField field = new IntField(testRunId, value);
                callback.momentaryReadOut(Collections.singletonList(field));
            }
        }).build();

        iotDataManagerOne.installThing(dataThing);

        List<IoTFieldsExtension> values;
        try {
            RosterIntegrationTest.ensureBothAccountsAreSubscribedToEachOther(conOne, conTwo, defaultTimeout);

            values = iotDataManagerTwo.requestMomentaryValuesReadOut(conOne.getUser());
        }
        finally {
            iotDataManagerOne.uninstallThing(dataThing);
            RosterIntegrationTest.ensureBothAccountsAreNotInEachOthersRoster(conOne, conTwo);
        }

        assertEquals(1, values.size());
    }
}
