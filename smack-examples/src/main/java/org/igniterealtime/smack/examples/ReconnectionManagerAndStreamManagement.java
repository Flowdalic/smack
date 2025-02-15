package org.igniterealtime.smack.examples;

import java.io.IOException;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.ReconnectionManager.ReconnectionPolicy;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.debugger.ConsoleDebugger;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.impl.JidCreate;

public class ReconnectionManagerAndStreamManagement {

    public static void reconnectionManagerAndStreamManagement(BareJid jid, String password)
                    throws XMPPException, SmackException, IOException, InterruptedException {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                        .setXmppAddressAndPassword(jid, password)
                        .setDebuggerFactory(ConsoleDebugger.Factory.INSTANCE)
                        .build();
        XMPPTCPConnection connection = new XMPPTCPConnection(config);

        var reconnectionManager = ReconnectionManager.getInstanceFor(connection);
        reconnectionManager.setReconnectionPolicy(ReconnectionPolicy.FIXED_DELAY);
        reconnectionManager.setFixedDelay(1);
        reconnectionManager.enableAutomaticReconnection();

        reconnectionManager.addReconnectionListener(new ReconnectionListener() {
            @Override
            public void reconnectingIn(int seconds) {
                System.out.println("Reconnecting in " + seconds + " seconds");
            }
            @Override
            public void reconnectionFailed(Exception e) {
                System.out.println("Reconnection failed: " + e);
                e.printStackTrace();
            }
        });
        connection.addConnectionListener(new ConnectionListener() {
            public void connected(XMPPConnection connection) {
                System.out.println("Connected");
            }
            public void authenticated(XMPPConnection connection, boolean resumed) {
                System.out.println("Authenticated");
            }
            public void connectionClosed() {
                System.out.println("Connection closed");
            }
            public void connectionClosedOnError(Exception e) {
                System.out.println("Connection closed on error: " + e);
                e.printStackTrace();
            }
        });

        connection.connect().login();
        Thread.sleep(10000000);
    }

    public static void main(String[] args) throws XMPPException, SmackException, IOException, InterruptedException {
        BareJid jid = JidCreate.bareFrom(args[0]);
        String password = args[1];
        reconnectionManagerAndStreamManagement(jid, password);
    }
}
