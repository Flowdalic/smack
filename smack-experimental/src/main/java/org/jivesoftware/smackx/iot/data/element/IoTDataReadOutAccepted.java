package org.jivesoftware.smackx.iot.data.element;

import org.jivesoftware.smack.packet.IQ;

public class IoTDataReadOutAccepted extends IQ {

    public static final String ELEMENT = "accepted";
    public static final String NAMESPACE = Constants.IOT_SENSORDATA_NAMESPACE;

    /**
     * The sequence nummber. According to XEP-0323 an xs:int.
     */
    private final int seqNr;

    private final boolean queued;

    protected IoTDataReadOutAccepted(int seqNr, boolean queued) {
        super(ELEMENT, NAMESPACE);
        this.seqNr = seqNr;
        this.queued = queued;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        // TODO Auto-generated method stub
        return null;
    }

}
