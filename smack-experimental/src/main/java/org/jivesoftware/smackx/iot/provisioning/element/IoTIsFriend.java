package org.jivesoftware.smackx.iot.provisioning.element;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

public class IoTIsFriend extends IQ {

    public static final String ELEMENT = "isFriend";
    public static final String NAMESPACE = Constants.IOT_PROVISIONING_NAMESPACE;

    private final Jid jid;

    public IoTIsFriend(Jid jid) {
        super(ELEMENT, NAMESPACE);
        this.jid = jid;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.attribute("jid", jid);
        return xml;
    }

}
