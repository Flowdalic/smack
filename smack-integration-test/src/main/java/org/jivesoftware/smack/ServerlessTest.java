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
package org.jivesoftware.smack;

import org.igniterealtime.smack.inttest.AbstractSmackLowLevelIntegrationTest;
import org.igniterealtime.smack.inttest.Configuration;
import org.igniterealtime.smack.inttest.SmackIntegrationTest;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;

public class ServerlessTest extends AbstractSmackLowLevelIntegrationTest {

    public static final String PRESENCE = "_presence._tcp.local.";

    public ServerlessTest(Configuration configuration, String testRunId) {
        super(configuration, testRunId);
    }

    @SmackIntegrationTest public void simpleTest() throws IOException, InterruptedException {
        LOGGER.info("Logger info is working");
        JmDNS jmdns = JmDNS.create();
        jmdns.addServiceListener(PRESENCE, new ServiceListener() {
            @Override public void serviceAdded(ServiceEvent event) {
                LOGGER.info(event.toString());
            }

            @Override public void serviceRemoved(ServiceEvent event) {
                LOGGER.info(event.toString());
            }

            @Override public void serviceResolved(ServiceEvent event) {
                LOGGER.info(event.toString());
            }
        });
        jmdns.registerService(ServiceInfo.create(PRESENCE, "juliet@pronto", 5562,
                        "Testing Service A"));
        jmdns.registerService(ServiceInfo.create(PRESENCE, "romeo@doronto", 5563,
                        "Testing Service B"));

        Thread.sleep(10000);

        for (ServiceInfo si : jmdns.list(PRESENCE)) {
            LOGGER.info(si.toString());
        }

        Thread.sleep(5000);
    }
}
