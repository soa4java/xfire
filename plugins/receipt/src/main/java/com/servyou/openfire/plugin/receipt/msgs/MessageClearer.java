package com.servyou.openfire.plugin.receipt.msgs;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

public abstract class MessageClearer {

	static Logger logger = LoggerFactory.getLogger(MessageClearer.class);

	static SessionManager sessionManager = SessionManager.getInstance();

	public static void remove(String msgId, JID toJid) {
		MessageQueueWaitingReceipt queue = MessageQueueMap.get(sessionManager.getSession(toJid));
		remove(queue, msgId);
	}

	public static void removeByToJID(Message message) {
		Collection<ClientSession> toSessions = SessionManager.getInstance().getSessions(message.getTo().getNode());
		if (CollectionUtils.isEmpty(toSessions)) {
			return;
		}

		for (ClientSession toClientSess : toSessions) {
			MessageQueueWaitingReceipt queue = MessageQueueMap.get(toClientSess);
			remove(queue, message.getID());
		}
		
	}
	
	public static void removeByFromJID(Message message) {
		MessageQueueWaitingReceipt queue = MessageQueueMap.get(sessionManager.getSession(message.getFrom()));
		remove(queue, message.getID());
	}

	public static void remove(MessageQueueWaitingReceipt queue, String msgId) {
		Message msg = queue.peek();
		
		if(msg == null){
			return;
		}
		
		if (msgId.equalsIgnoreCase(msg.getID())) {
			queue.poll();
			queue.setCount(0);
			queue.setTimestamp(System.currentTimeMillis());
		} else {
			logger.warn("msg dequeu not normal!!");

			Iterator<Message> itr = queue.iterator();
			while (itr.hasNext()) {
				msg = itr.next();
				if (msgId.equalsIgnoreCase(msg.getID())) {
					queue.remove(msg);
					queue.setCount(0);
					queue.setTimestamp(System.currentTimeMillis());
					break;
				}
			}
		}
	}
}
