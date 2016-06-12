package org.jivesoftware.smackx.iot.data.element;

import org.jivesoftware.smack.packet.IQ;

public class IoTDataRequest extends IQ {

    public static final String ELEMENT = "req";
    public static final String NAMESPACE = Constants.IOT_SENSORDATA_NAMESPACE;

    /**
     * The sequence nummber. According to XEP-0323 an xs:int.
     */
    private final int seqNr;

    private final boolean momentary;

    protected IoTDataRequest(int seqNr, boolean momentary) {
        super(ELEMENT, NAMESPACE);
        this.seqNr = seqNr;
        this.momentary = momentary;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        // TODO Auto-generated method stub
        return null;
    }

}
