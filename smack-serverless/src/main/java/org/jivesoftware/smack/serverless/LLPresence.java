/**
 *
 * Copyright 2015 Ishan Khanna
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
package org.jivesoftware.smack.serverless;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for describing a Link-local presence information according to XEP-0174.
 * XEP-0174 describes how to represent XMPP presences using mDNS/DNS-SD.
 * The presence information is stored as TXT fields; example from the documentation
 * follows:
 * <pre>
 *        juliet IN TXT "txtvers=1"
 *        juliet IN TXT "1st=Juliet"
 *        juliet IN TXT "email=juliet@capulet.lit"
 *        juliet IN TXT "hash=sha-1"
 *        juliet IN TXT "jid=juliet@capulet.lit"
 *        juliet IN TXT "last=Capulet"
 *        juliet IN TXT "msg=Hanging out downtown"
 *        juliet IN TXT "nick=JuliC"
 *        juliet IN TXT "node=http://www.adiumx.com"
 *        juliet IN TXT "phsh=a3839614e1a382bcfebbcf20464f519e81770813"
 *        juliet IN TXT "port.p2pj=5562"
 *        juliet IN TXT "status=avail"
 *        juliet IN TXT "vc=CA!"
 *        juliet IN TXT "ver=66/0NaeaBKkwk85efJTGmU47vXI="
 * </pre>
 */

public class LLPresence {

    // Additional properties (if any)
    private final Map<String, String> additionalProperties = new HashMap<String, String>();
    // Information about the User gathered from TXT records
    private String firstName;
    private String lastName;
    private String email;
    private String msg;
    private String nick;
    private String jid;
    // Capabilities version information
    private String hash;
    private String ver;
    private String node;
    /**
     * As per the documentation, by default the presence status of a link-local
     * user will be Available.
     */
    private Mode status = Mode.AVAILABLE;
    // Host Details
    private int port = 0;
    private String host;
    private String serviceName;
    public LLPresence(String serviceName) {
        this.serviceName = serviceName;
    }

    public LLPresence(String serviceName, String host, int port) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
    }

    public LLPresence(String serviceName, String host, int port, Map<String, String> records) {
        this(serviceName, host, port);

        // Parse the map (originating from the TXT fields) and put them
        // in variables
        for (Map.Entry<String, String> entry : records.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            switch (key) {
            case "1st":
                setFirstName(value);
                break;
            case "last":
                setLastName(value);
                break;
            case "email":
                setEmail(value);
                break;
            case "jid":
                setJid(value);
                break;
            case "nick":
                setNick(value);
                break;
            case "hash":
                setHash(value);
                break;
            case "node":
                setNode(value);
                break;
            case "ver":
                setVer(value);
                break;
            case "status":
                setStatus(Mode.valueOf(value));
                break;
            case "msg":
                setMsg(value);
                break;
            default:
                additionalProperties.put(key, value);
            }
        }

    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>(additionalProperties.size() + 20);
        map.put("txtvers", "1");
        map.put("1st", firstName);
        map.put("last", lastName);
        map.put("email", email);
        map.put("jid", jid);
        map.put("nick", nick);
        map.put("status", status.toString());
        map.put("msg", msg);
        map.put("hash", hash);
        map.put("node", node);
        map.put("ver", ver);
        map.put("port.p2ppj", Integer.toString(port));

        map.putAll(additionalProperties);

        return map;
    }

    /**
     * Update all the values of the presence.
     */
    void update(LLPresence p) {
        setFirstName(p.getFirstName());
        setLastName(p.getLastName());
        setEmail(p.getEmail());
        setMsg(p.getMsg());
        setNick(p.getNick());
        setStatus(p.getStatus());
        setJid(p.getJid());
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public Mode getStatus() {
        return status;
    }

    public void setStatus(Mode status) {
        this.status = status;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public static enum Mode {
        AVAILABLE,
        AWAY,
        DND,
        BUSY
    }
}
