/**
 *
 * Copyright 2015 Ishan Khanna.
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
    
    // Additional properties (if any)
    private final Map<String, String> additionalProperties = new HashMap<String, String>();
    
    public static enum Mode {
        AVAILABLE,
        AWAY,
        DND,
        BUSY
    }
    
    /**
     * As per the documentation, by default the presence status of a link-local 
     * user will be Available.
     */
    private Mode status = Mode.AVAILABLE;
    
    // Host Details
    private int port = 0;
    private String host;
    private String serviceName;
    
    
}
