package com.servyou.openfire.plugin.receipt.msgs;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.xmpp.packet.Message;

public class MessageQueueWaitingReceipt extends ConcurrentLinkedQueue<Message> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1658115759487532174L;
	private long timestamp;

	public synchronized long getTimestamp() {
		return timestamp;
	}

	public synchronized void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
