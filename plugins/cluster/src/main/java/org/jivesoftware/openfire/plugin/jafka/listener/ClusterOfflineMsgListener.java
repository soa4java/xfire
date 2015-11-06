package org.jivesoftware.openfire.plugin.jafka.listener;

import org.jivesoftware.of.common.message.PacketQueue;
import org.jivesoftware.openfire.OfflineMessageListener;
import org.xmpp.packet.Message;

public class ClusterOfflineMsgListener implements OfflineMessageListener {

	PacketQueue packetQueue = PacketQueue.getInstance();
	
	public void messageDeleted(Message message) {
		packetQueue.enQueue(message);
	}

	@Override
	public void messageBounced(Message message) {
		packetQueue.enQueue(message);
	}

	@Override
	public void messageStored(Message message) {
		packetQueue.enQueue(message);
	}

}
