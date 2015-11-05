package org.jivesoftware.openfire.plugin.xroster.receipt.msgs;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.xmpp.packet.Message;

public class MessageQueueWaitingReceipt extends ConcurrentLinkedQueue<Message> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1658115759487532174L;
	private long timestamp;
	private int count;

	public synchronized long getTimestamp() {
		return timestamp;
	}

	public synchronized void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public synchronized int getCount() {
		return count;
	}

	public synchronized void setCount(int count) {
		this.count = count;
	}

	public synchronized void countIncrease(int x) {
		this.count += x;
	}

}
