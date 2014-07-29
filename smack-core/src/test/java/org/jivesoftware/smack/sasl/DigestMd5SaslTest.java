/**
 *
 * Copyright © 2014 Florian Schmaus
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
package org.jivesoftware.smack.sasl;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.util.StringUtils;

public class DigestMd5SaslTest extends AbstractSaslTest {

    protected static final String challenge = "realm=\"xmpp.org\",nonce=\"aTUr3GXqUtyy2B7HVDW6C+gQs+j+0EhWWjoBKkkg\",qop=\"auth\",charset=utf-8,algorithm=md5-sess";
    protected static final byte[] challengeBytes = StringUtils.toBytes(challenge);

    public DigestMd5SaslTest(SASLMechanism saslMechanism) {
        super(saslMechanism);
    }

    public String runTest() throws NotConnectedException, SmackException {
        saslMechanism.authenticate("florian", "irrelevant", "xmpp.org", "secret");

        byte[] response = saslMechanism.evaluateChallenge(challengeBytes);
        return new String(response);
    }
}
