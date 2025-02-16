/**
 *
 * Copyright 2021-2024 Florian Schmaus
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

package org.jivesoftware.smackx.muc;

import java.util.List;
import java.util.logging.Level;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.StanzaError;
import org.jivesoftware.smack.util.StringUtils;

import org.jivesoftware.smackx.muc.MultiUserChatException.MissingMucCreationAcknowledgeException;
import org.jivesoftware.smackx.muc.MultiUserChatException.MucAlreadyJoinedException;
import org.jivesoftware.smackx.muc.MultiUserChatException.NotAMucServiceException;
import org.jivesoftware.smackx.xdata.form.FillableForm;
import org.jivesoftware.smackx.xdata.form.Form;

import org.igniterealtime.smack.inttest.AbstractSmackIntegrationTest;
import org.igniterealtime.smack.inttest.SmackIntegrationTestEnvironment;
import org.igniterealtime.smack.inttest.TestNotPossibleException;

import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;


public abstract class AbstractMultiUserChatIntegrationTest extends AbstractSmackIntegrationTest {

    final String randomString = StringUtils.insecureRandomString(6);

    final MultiUserChatManager mucManagerOne;
    final MultiUserChatManager mucManagerTwo;
    final MultiUserChatManager mucManagerThree;
    final DomainBareJid mucService;

    public AbstractMultiUserChatIntegrationTest(SmackIntegrationTestEnvironment environment)
            throws SmackException.NoResponseException, XMPPException.XMPPErrorException, SmackException.NotConnectedException,
            InterruptedException, TestNotPossibleException, MucAlreadyJoinedException, MissingMucCreationAcknowledgeException, NotAMucServiceException, XmppStringprepException {
        super(environment);
        mucManagerOne = MultiUserChatManager.getInstanceFor(conOne);
        mucManagerTwo = MultiUserChatManager.getInstanceFor(conTwo);
        mucManagerThree = MultiUserChatManager.getInstanceFor(conThree);

        List<DomainBareJid> services = mucManagerOne.getMucServiceDomains();
        if (services.isEmpty()) {
            throw new TestNotPossibleException("No MUC (XEP-0045) service found");
        }

        DomainBareJid needle = null;
        for (final DomainBareJid service : services) {
            MultiUserChat multiUserChat = null;
            try {
                String roomNameLocal = String.join("-", "smack-inttest-abstract", testRunId, StringUtils.insecureRandomString(6));
                EntityBareJid mucAddress = JidCreate.entityBareFrom(Localpart.from(roomNameLocal), service.getDomain());
                multiUserChat = mucManagerOne.getMultiUserChat(mucAddress);

                createMuc(multiUserChat, "test");

                needle = service;
                break;
            } catch (XMPPException.XMPPErrorException e) {
                mucCreationDisallowedOrThrow(e);
                LOGGER.log(Level.FINER, "MUC service " + service + " does not allow MUC creation", e);
            } finally {
                tryDestroy(multiUserChat);
            }
        }

        if (needle == null) {
            throw new TestNotPossibleException("No MUC (XEP-0045) service found that allows test users to createa new room. Considered MUC services: " + services);
        }
        mucService = needle;
    }

    static void mucCreationDisallowedOrThrow(XMPPException.XMPPErrorException e) throws XMPPErrorException {
        StanzaError.Condition condition = e.getStanzaError().getCondition();
        if (condition == StanzaError.Condition.not_allowed)
            return;
        throw e;
    }

    /**
     * Gets a random room name.
     *
     * @param prefix A prefix to add to the room name for descriptive purposes
     * @return the bare JID of a random room
     * @throws XmppStringprepException if the prefix isn't a valid XMPP Localpart
     */
    public EntityBareJid getRandomRoom(String prefix) throws XmppStringprepException {
        final String roomNameLocal = String.join("-", "sinttest", prefix, testRunId, StringUtils.insecureRandomString(3));
        return JidCreate.entityBareFrom(Localpart.from(roomNameLocal), mucService.getDomain());
    }

    /**
     * Destroys a MUC room, ignoring any exceptions.
     *
     * @param muc The room to destroy (can be null).
     * @throws InterruptedException if the calling thread was interrupted.
     * @throws SmackException.NotConnectedException if the XMPP connection is not connected.
     * @throws XMPPException.XMPPErrorException if there was an XMPP error returned.
     * @throws SmackException.NoResponseException if there was no response from the remote entity.
     */
    static void tryDestroy(final MultiUserChat muc) throws SmackException.NoResponseException, XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException {
        if (muc == null) {
            return;
        }
        muc.destroy("test fixture teardown", null);
    }

    static void createMuc(MultiUserChat muc, Resourcepart resourceName) throws
            SmackException.NoResponseException, XMPPException.XMPPErrorException,
            InterruptedException, MultiUserChatException.MucAlreadyJoinedException,
            SmackException.NotConnectedException,
            MultiUserChatException.MissingMucCreationAcknowledgeException,
            MultiUserChatException.NotAMucServiceException {
        muc.create(resourceName).makeInstant();
    }

    static void createMuc(MultiUserChat muc, String nickname) throws
            XmppStringprepException, MultiUserChatException.MucAlreadyJoinedException,
            XMPPException.XMPPErrorException, SmackException.NotConnectedException,
            MultiUserChatException.MissingMucCreationAcknowledgeException,
            SmackException.NoResponseException, InterruptedException,
            MultiUserChatException.NotAMucServiceException {
        createMuc(muc, Resourcepart.from(nickname));
    }

    static void createMembersOnlyMuc(MultiUserChat muc, Resourcepart resourceName) throws
            SmackException.NoResponseException, XMPPException.XMPPErrorException,
            InterruptedException, MultiUserChatException.MucAlreadyJoinedException,
            SmackException.NotConnectedException,
            MultiUserChatException.MissingMucCreationAcknowledgeException,
            MultiUserChatException.MucConfigurationNotSupportedException,
            MultiUserChatException.NotAMucServiceException {
        muc.create(resourceName)
            .getConfigFormManager()
            .makeMembersOnly()
            .submitConfigurationForm();
    }

    static void createModeratedMuc(MultiUserChat muc, Resourcepart resourceName)
                    throws SmackException.NoResponseException, XMPPException.XMPPErrorException, InterruptedException,
                    MultiUserChatException.MucAlreadyJoinedException, SmackException.NotConnectedException,
                    MultiUserChatException.MissingMucCreationAcknowledgeException,
                    MultiUserChatException.NotAMucServiceException,
                    MultiUserChatException.MucConfigurationNotSupportedException {
        muc.create(resourceName)
            .getConfigFormManager()
            .makeModerated()
            .submitConfigurationForm();
    }

    static void createHiddenMuc(MultiUserChat muc, Resourcepart resourceName)
                    throws SmackException.NoResponseException, XMPPException.XMPPErrorException, InterruptedException,
                    MultiUserChatException.MucAlreadyJoinedException, SmackException.NotConnectedException,
                    MultiUserChatException.MissingMucCreationAcknowledgeException, MultiUserChatException.NotAMucServiceException, XmppStringprepException,
                    MultiUserChatException.MucConfigurationNotSupportedException {
        muc.create(resourceName)
            .getConfigFormManager()
            .makeHidden()
            .submitConfigurationForm();
    }

    /**
     * Creates a non-anonymous room.
     *
     * <p>From XEP-0045 § 10.1.3:</p>
     * <blockquote>
     * Note: The _whois configuration option specifies whether the room is non-anonymous (a value of "anyone"),
     * semi-anonymous (a value of "moderators"), or fully anonymous (a value of "none", not shown here).
     * </blockquote>
     */
    static void createNonAnonymousMuc(MultiUserChat muc, Resourcepart resourceName) throws SmackException.NoResponseException, XMPPException.XMPPErrorException, InterruptedException, MultiUserChatException.MucAlreadyJoinedException, SmackException.NotConnectedException, MultiUserChatException.MissingMucCreationAcknowledgeException, MultiUserChatException.NotAMucServiceException {
        muc.create(resourceName);
        Form configForm = muc.getConfigurationForm();
        FillableForm answerForm = configForm.getFillableForm();
        answerForm.setAnswer("muc#roomconfig_whois", "anyone");
        muc.sendConfigurationForm(answerForm);
    }

    /**
     * Creates a semi-anonymous room.
     *
     * <p>From XEP-0045 § 10.1.3:</p>
     * <blockquote>
     * Note: The _whois configuration option specifies whether the room is non-anonymous (a value of "anyone"),
     * semi-anonymous (a value of "moderators"), or fully anonymous (a value of "none", not shown here).
     * </blockquote>
     */
    static void createSemiAnonymousMuc(MultiUserChat muc, Resourcepart resourceName) throws SmackException.NoResponseException, XMPPException.XMPPErrorException, InterruptedException, MultiUserChatException.MucAlreadyJoinedException, SmackException.NotConnectedException, MultiUserChatException.MissingMucCreationAcknowledgeException, MultiUserChatException.NotAMucServiceException {
        muc.create(resourceName);
        Form configForm = muc.getConfigurationForm();
        FillableForm answerForm = configForm.getFillableForm();
        answerForm.setAnswer("muc#roomconfig_whois", "moderators");
        muc.sendConfigurationForm(answerForm);
    }

    /**
     * Creates a password-protected room.
     */
    static void createPasswordProtectedMuc(MultiUserChat muc, Resourcepart resourceName, String password) throws SmackException.NoResponseException, XMPPException.XMPPErrorException, InterruptedException, MultiUserChatException.MucAlreadyJoinedException, SmackException.NotConnectedException, MultiUserChatException.MissingMucCreationAcknowledgeException, MultiUserChatException.NotAMucServiceException {
        muc.create(resourceName);
        Form configForm = muc.getConfigurationForm();
        FillableForm answerForm = configForm.getFillableForm();
        answerForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
        answerForm.setAnswer("muc#roomconfig_roomsecret", password);
        muc.sendConfigurationForm(answerForm);
    }

    static void setMaxUsers(MultiUserChat muc, int maxUsers) throws SmackException.NoResponseException, XMPPException.XMPPErrorException, InterruptedException, SmackException.NotConnectedException {
        Form configForm = muc.getConfigurationForm();
        FillableForm answerForm = configForm.getFillableForm();
        answerForm.setAnswer("muc#roomconfig_maxusers", maxUsers);
        muc.sendConfigurationForm(answerForm);
    }

}
