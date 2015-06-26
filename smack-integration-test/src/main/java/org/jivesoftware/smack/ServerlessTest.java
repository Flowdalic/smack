package org.jivesoftware.smack;

import org.igniterealtime.smack.inttest.SmackIntegrationTest;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;

/**
 * Created by ishan on 25/06/15.
 */
public class ServerlessTest {

    @SmackIntegrationTest
    public void simpleTest() throws IOException, InterruptedException {

        JmDNS jmdns = JmDNS.create();
        jmdns.registerService(ServiceInfo.create("_presence._tcp.local.", "juliet@pronto._presence._tcp.local.", 5562, "Testing Service A"));
        jmdns.registerService(ServiceInfo.create("_presence._tcp.local.", "romeo@doronto._presence._tcp.local.", 5563, "Testing Service B"));

        jmdns.addServiceListener("_presence._tcp.local.", new ServiceListener() {
            @Override
            public void serviceAdded(ServiceEvent event) {
                System.out.println(event.getInfo().getName());

            }

            @Override
            public void serviceRemoved(ServiceEvent event) {

            }

            @Override
            public void serviceResolved(ServiceEvent event) {

            }
        });
        Thread.sleep(10000);
    }
}
