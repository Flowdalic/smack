package org.jivesoftware.smack;

import org.igniterealtime.smack.inttest.AbstractSmackLowLevelIntegrationTest;
import org.igniterealtime.smack.inttest.Configuration;
import org.igniterealtime.smack.inttest.SmackIntegrationTest;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ishan on 25/06/15.
 */
public class ServerlessTest extends AbstractSmackLowLevelIntegrationTest{

    public static final String PRESENCE = "_presence._tcp.local.";

    public ServerlessTest(Configuration configuration, String testRunId) {
        super(configuration, testRunId);
    }

    @SmackIntegrationTest
    public void simpleTest() throws IOException, InterruptedException {
        LOGGER.info("Logger info is working");
        JmDNS jmdns = JmDNS.create();
        jmdns.addServiceListener(PRESENCE, new ServiceListener() {
            @Override
            public void serviceAdded(ServiceEvent event) {
                LOGGER.info(event.toString());
            }

            @Override
            public void serviceRemoved(ServiceEvent event) {

            }

            @Override
            public void serviceResolved(ServiceEvent event) {

            }
        });
        jmdns.registerService(ServiceInfo.create("_presence._tcp.local.", "juliet@pronto._presence._tcp.local.", 5562, "Testing Service A"));
        Thread.sleep(10000);
        jmdns.registerService(ServiceInfo.create("_presence._tcp.local.", "romeo@doronto._presence._tcp.local.", 5563, "Testing Service B"));
        Thread.sleep(10000);
        for (ServiceInfo si : jmdns.list(PRESENCE)) {
            LOGGER.info(si.toString());
        }
        Thread.sleep(5000);
    }
}
