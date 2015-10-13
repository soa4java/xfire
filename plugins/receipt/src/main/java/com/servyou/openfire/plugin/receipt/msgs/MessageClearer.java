package com.servyou.openfire.plugin.receipt.msgs;

import java.util.Iterator;

import org.jivesoftware.openfire.SessionManager;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

public abstract class MessageClearer {

	static SessionManager sessionManager = SessionManager.getInstance();

	public static void remove(String msgId, JID toJid) {
		MessageQueueWaitingReceipt queue = MessageQueueMap.get(sessionManager.getSession(toJid));
		remove(queue, msgId);
	}

	public static void remove(Message message) {
		MessageQueueWaitingReceipt queue = MessageQueueMap.get(sessionManager.getSession(message.getFrom()));
		remove(queue, message.getID());
	}

	public static void remove(MessageQueueWaitingReceipt queue, String msgId) {
		Message msg = queue.peek();
		if (msgId.equalsIgnoreCase(msg.getID())) {
			queue.poll();
		}

		Iterator<Message> itr = queue.iterator();
		while (itr.hasNext()) {
			msg = itr.next();
			if (msgId.equalsIgnoreCase(msg.getID())) {
				queue.remove(msg);
			}
		}
	}
}
