/**
 *
 * Copyright © 2016 Florian Schmaus
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
