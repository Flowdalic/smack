package org.jivesoftware.smackx.iot.discovery.element;

import org.jivesoftware.smack.packet.NamedElement;
import org.jivesoftware.smack.util.Objects;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smack.util.XmlStringBuilder;

public class Tag implements NamedElement {

    public enum Type {
        str,
        num;
    }

    private final String name;
    private final Type type;
    private final String value;

    public Tag(String name, Type type, String value) {
        this.name = StringUtils.requireNotNullOrEmpty(name, "name must not be null or empty");
        this.type = Objects.requireNonNull(type);
        this.value =  StringUtils.requireNotNullOrEmpty(value, "value must not be null or empty");
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public XmlStringBuilder toXML() {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        return xml;
    }

    @Override
    public String getElementName() {
        return getType().toString();
    }
}
