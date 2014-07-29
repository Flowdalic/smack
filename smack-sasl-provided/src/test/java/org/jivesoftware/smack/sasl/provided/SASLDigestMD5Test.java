/**
 *
 * Copyright 2014 Florian Schmaus
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
package org.jivesoftware.smack.sasl.provided;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.sasl.DigestMd5SaslTest;
import org.jivesoftware.smack.sasl.provided.SASLDigestMD5Mechanism;
import org.junit.Test;

public class SASLDigestMD5Test {
    @Test
    public void testDigestMD5() throws NotConnectedException, SmackException {
        DigestMd5SaslTest test = new DigestMd5SaslTest(new SASLDigestMD5Mechanism());
        String response = test.runTest();
    }
}
