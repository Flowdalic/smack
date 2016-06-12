package org.jivesoftware.smackx.iot.provisioning.element;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

public class IoTIsFriendResponse extends IQ {

    public static final String ELEMENT = "isFriend";
    public static final String NAMESPACE = Constants.IOT_PROVISIONING_NAMESPACE;

    private final Jid jid;

    private final boolean result;

    public IoTIsFriendResponse(Jid jid, boolean result) {
        super(ELEMENT, NAMESPACE);
        this.jid = jid;
        this.result = result;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.attribute("jid", jid);
        xml.attribute("result", result);
        return xml;
    }

}
