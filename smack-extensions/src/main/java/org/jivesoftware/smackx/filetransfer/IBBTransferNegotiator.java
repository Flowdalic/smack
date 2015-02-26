/**
 *
 * Copyright 2003-2006 Jive Software.
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
package org.jivesoftware.smackx.filetransfer;

import java.io.InputStream;
import java.io.OutputStream;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FlexibleStanzaTypeFilter;
import org.jivesoftware.smack.filter.FromMatchesFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.util.Objects;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamRequest;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamSession;
import org.jivesoftware.smackx.bytestreams.ibb.packet.DataPacketExtension;
import org.jivesoftware.smackx.bytestreams.ibb.packet.Open;
import org.jivesoftware.smackx.si.packet.StreamInitiation;

/**
 * The In-Band Bytestream file transfer method, or IBB for short, transfers the
 * file over the same XML Stream used by XMPP. It is the fall-back mechanism in
 * case the SOCKS5 bytestream method of transferring files is not available.
 * 
 * @author Alexander Wenckus
 * @author Henning Staib
 * @see <a href="http://xmpp.org/extensions/xep-0047.html">XEP-0047: In-Band
 *      Bytestreams (IBB)</a>
 */
public class IBBTransferNegotiator extends StreamNegotiator {

    private XMPPConnection connection;

    private InBandBytestreamManager manager;

    /**
     * The default constructor for the In-Band Bytestream Negotiator.
     * 
     * @param connection The connection which this negotiator works on.
     */
    protected IBBTransferNegotiator(XMPPConnection connection) {
        this.connection = connection;
        this.manager = InBandBytestreamManager.getByteStreamManager(connection);
    }

    public OutputStream createOutgoingStream(String streamID, String initiator,
                    String target) throws NoResponseException, XMPPErrorException, NotConnectedException {
        InBandBytestreamSession session = this.manager.establishSession(target, streamID);
        session.setCloseBothStreamsEnabled(true);
        return session.getOutputStream();
    }

    public InputStream createIncomingStream(StreamInitiation initiation)
                    throws NoResponseException, XMPPErrorException, NotConnectedException {
        /*
         * In-Band Bytestream initiation listener must ignore next in-band bytestream request with
         * given session ID
         */
        this.manager.ignoreBytestreamRequestOnce(initiation.getSessionID());

        Stanza streamInitiation = initiateIncomingStream(this.connection, initiation);
        return negotiateIncomingStream(streamInitiation);
    }

    public StanzaFilter getInitiationPacketFilter(String from, String streamID) {
        /*
         * this method is always called prior to #negotiateIncomingStream() so
         * the In-Band Bytestream initiation listener must ignore the next
         * In-Band Bytestream request with the given session ID
         */
        this.manager.ignoreBytestreamRequestOnce(streamID);

        return new AndFilter(FromMatchesFilter.create(from), new IBBOpenSidFilter(streamID));
    }

    public String[] getNamespaces() {
        return new String[] { DataPacketExtension.NAMESPACE };
    }

    InputStream negotiateIncomingStream(Stanza streamInitiation) throws NotConnectedException {
        // build In-Band Bytestream request
        InBandBytestreamRequest request = new ByteStreamRequest(this.manager,
                        (Open) streamInitiation);

        // always accept the request
        InBandBytestreamSession session = request.accept();
        session.setCloseBothStreamsEnabled(true);
        return session.getInputStream();
    }

    /**
     * This PacketFilter accepts an incoming In-Band Bytestream open request
     * with a specified session ID.
     */
    private static class IBBOpenSidFilter extends FlexibleStanzaTypeFilter<Open> {

        private final String sessionID;

        public IBBOpenSidFilter(String sessionID) {
            this.sessionID = Objects.requireNonNull(sessionID, "sessionID must not be null");
        }

        @Override
        protected boolean acceptSpecific(Open bytestream) {
            // packet must by of type SET and contains the given session ID
            return this.sessionID.equals(bytestream.getSessionID())
                            && IQ.Type.set.equals(bytestream.getType());
        }
    }

    /**
     * Derive from InBandBytestreamRequest to access protected constructor.
     */
    private static class ByteStreamRequest extends InBandBytestreamRequest {

        private ByteStreamRequest(InBandBytestreamManager manager, Open byteStreamRequest) {
            super(manager, byteStreamRequest);
        }

    }

}
