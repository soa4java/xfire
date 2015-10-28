package org.jivesoftware.openfire.plugin.xroster.extcontacts.internal.receipt.listener;

import org.jivesoftware.of.common.utils.SessionUtils;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.user.PresenceEventListener;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

public class ReceiptPresenceEventListener implements PresenceEventListener {

	@Override
	public void availableSession(ClientSession session, Presence presence) {
		SessionUtils.receiptEnable(session, true);
	}

	@Override
	public void unavailableSession(ClientSession session, Presence presence) {

	}

	@Override
	public void presenceChanged(ClientSession session, Presence presence) {

	}

	@Override
	public void subscribedToPresence(JID subscriberJID, JID authorizerJID) {

	}

	@Override
	public void unsubscribedToPresence(JID unsubscriberJID, JID recipientJID) {

	}

}
