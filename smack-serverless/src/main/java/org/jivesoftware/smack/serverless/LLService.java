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

import java.util.List;

import org.jxmpp.jid.BareJid;

public interface LLService {
    
    
    /**
     * Fetch list of all the users who are present on the Link Local network.
     * 
     * @return List<BareJid> list of BareJid(s) of all available clients
     */
    public List<BareJid> getAllClientsPresentOnLLNetwork();
    
    
    /**
     * Marks presence of a client on the Link Local Network. Marking presence
     * on the network does not mean client is available for chat. It might so
     * happen that client is present on the network but is Away, Busy or 
     * Unavailable for chat. 
     * 
     * @param bareJid BareJid of the client
     * @return true if client was able to broadcast presence on the network
     * successfully and false otherwise
     */
    public boolean markClientPresentOnLLNetwork(BareJid bareJid);
    
    /**
    * Marks absence of a client on the Link Local Network. Marking absence
    * on the network means client is no longer connected to the Link Local 
    * Network, in simpler terms client has logged out. 
    * 
    * @param bareJid BareJid of the client
    * @return true if client was able to broadcast absence on the network
    * successfully and false otherwise
    */
    public boolean markClientAbsentOnLLNetwork(BareJid bareJid);
    
}
