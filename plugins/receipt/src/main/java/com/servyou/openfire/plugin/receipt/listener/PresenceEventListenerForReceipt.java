package com.servyou.openfire.plugin.receipt.listener;

import org.apache.commons.collections.CollectionUtils;
import org.jivesoftware.of.common.thread.XExecutor;
import org.jivesoftware.openfire.OfflineMessageStrategy;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.user.PresenceEventListener;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Presence;

import com.servyou.openfire.plugin.receipt.msgs.MessageQueueMap;
import com.servyou.openfire.plugin.receipt.msgs.MessageQueueWaitingReceipt;

public class PresenceEventListenerForReceipt implements PresenceEventListener {

	private OfflineMessageStrategy messageStrategy = XMPPServer.getInstance().getOfflineMessageStrategy();;

	@Override
	public void availableSession(ClientSession session, Presence presence) {

	}

	@Override
	public void unavailableSession(ClientSession session, Presence presence) {
		MessageQueueWaitingReceipt queue = MessageQueueMap.remove(session);
		if (CollectionUtils.isNotEmpty(queue))
			for (final Message msg : queue) {
				XExecutor.globalExecutor.submit(new Runnable() {
					@Override
					public void run() {
						messageStrategy.storeOffline(msg);
					}
				});
			}

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
