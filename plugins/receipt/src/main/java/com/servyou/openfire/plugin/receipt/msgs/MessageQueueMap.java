package com.servyou.openfire.plugin.receipt.msgs;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jivesoftware.openfire.session.ClientSession;
import org.xmpp.packet.Message;

public class MessageQueueMap {
	private static ConcurrentHashMap<ClientSession, MessageQueueWaitingReceipt> map = new ConcurrentHashMap<ClientSession, MessageQueueWaitingReceipt>();

	public static MessageQueueWaitingReceipt get(ClientSession session) {
		MessageQueueWaitingReceipt queue = map.get(session);
		if(null == queue){
			queue = new MessageQueueWaitingReceipt();
			map.put(session, queue);
		}
		return queue;
	}
	
	public static MessageQueueWaitingReceipt remove(ClientSession session) {
		 return map.remove(session);
	}

	public static ConcurrentLinkedQueue<Message> put(ClientSession session, Message message) {
		MessageQueueWaitingReceipt queue = get(session);
		if (queue == null) {
			queue = new MessageQueueWaitingReceipt();
		}
		queue.setCount(0);
		queue.setTimestamp(System.currentTimeMillis());
		queue.add(message);
		return map.put(session, queue);
	}

}
