package org.jivesoftware.openfire.plugin.xroster.extcontacts.internal.receipt.msgs;

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
		ClientSession session = sessionManager.getSession(toJid);
		if(session == null){
			return;
		}
		MessageQueueWaitingReceipt queue = MessageQueueMap.get(session);
		remove(queue, msgId);
	}

	public static void removeByToBaredJID(Message message) {
		Collection<ClientSession> toSessions = SessionManager.getInstance().getSessions(message.getTo().getNode());
		if (CollectionUtils.isEmpty(toSessions)) {
			return;
		}

		for (ClientSession toClientSess : toSessions) {
			MessageQueueWaitingReceipt queue = MessageQueueMap.get(toClientSess);
			remove(queue, message.getID());
		}
		
	}
	
	public static void removeByFromFullJID(Message message) {
		ClientSession session = sessionManager.getSession(message.getFrom());
		
		if(session == null){
			return;
		}
		
		MessageQueueWaitingReceipt queue = MessageQueueMap.get(session);
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
			logger.warn("msg dequeue not normal !!");

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
