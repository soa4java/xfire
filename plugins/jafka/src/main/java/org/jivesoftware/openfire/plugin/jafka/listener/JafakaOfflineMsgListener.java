package org.jivesoftware.openfire.plugin.jafka.listener;

import org.jivesoftware.openfire.OfflineMessageListener;
import org.jivesoftware.openfire.plugin.jafka.vo.PacketQueue;
import org.xmpp.packet.Message;

public class JafakaOfflineMsgListener implements OfflineMessageListener {

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
