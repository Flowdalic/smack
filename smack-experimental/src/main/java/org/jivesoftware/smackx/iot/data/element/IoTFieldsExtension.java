package org.jivesoftware.smackx.iot.data.element;

import org.jivesoftware.smack.packet.ExtensionElement;

public class IoTFieldsExtension implements ExtensionElement {

    public static final String ELEMENT = "fields";
    public static final String NAMESPACE = Constants.IOT_SENSORDATA_NAMESPACE;

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public CharSequence toXML() {
        // TODO Auto-generated method stub
        return null;
    }
}
