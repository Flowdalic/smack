/**
 *
 * Copyright 2016 Florian Schmaus
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

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

public class IoTClaimed extends IQ {

    public static final String ELEMENT = "remove";
    public static final String NAMESPACE = Constants.IOT_DISCOVERY_NAMESPACE;

    private final Jid jid;

    private final String nodeId;

    private final String sourceId;

    public IoTClaimed(Jid jid) {
        this(jid, null, null);
    }

    public IoTClaimed(Jid jid, String nodeId, String sourceId) {
        super(ELEMENT, NAMESPACE);
        this.jid = jid;
        this.nodeId = nodeId;
        this.sourceId = sourceId;
    }

    public Jid getJid() {
        return jid;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getSourceId() {
        return sourceId;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.attribute("jid", jid);
        xml.optAttribute("nodeId", nodeId);
        xml.optAttribute("sourceId", sourceId);
        xml.closeEmptyElement();
        return xml;
    }

}
