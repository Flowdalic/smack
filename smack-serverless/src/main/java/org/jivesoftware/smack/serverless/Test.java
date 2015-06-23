package org.jivesoftware.smack.serverless;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by ishan on 20/06/15.
 */
public class Test {

    public static void main(String[] args) throws IOException {
//        JmDNSService llService = new JmDNSService();
//        LLPresence llPresence = new LLPresence("julia@pronto");
//        llPresence.setPort(5288);
//        llPresence.setEmail("ishan1604@gmail.com");
//        llPresence.setFirstName("Ishan");
//        llPresence.setLastName("Khanna");
//        llPresence.setNick("ishan1604");
//        llPresence.setJid("ishan1604@jabb3r.net");
//        llPresence.setStatus(LLPresence.Mode.AVAILABLE);
//        try {
//            llPresence.setHost(InetAddress.getLocalHost().toString());
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        try {
//            llService.init();
//            llService.announcePresence(llPresence);
//        } catch (XMPPException e) {
//            e.printStackTrace();
//        }

        JmDNS jmdns = JmDNS.create();
        jmdns.registerService(ServiceInfo.create("_presence._tcp.local.", "juliet@pronto._presence._tcp.local.", 5562, "Testing Service"));
    }
}
