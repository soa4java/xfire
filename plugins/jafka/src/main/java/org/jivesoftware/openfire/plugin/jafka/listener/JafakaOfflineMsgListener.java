package org.jivesoftware.openfire.plugin.jafka.listener;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.collections.CollectionUtils;
import org.jivesoftware.openfire.OfflineMessageListener;
import org.jivesoftware.openfire.plugin.jafka.cache.UserNodeCache;
import org.jivesoftware.openfire.plugin.jafka.cache.impl.redis.RedisUserNodeCacheImpl;
import org.jivesoftware.openfire.plugin.jafka.service.OfflineMessageService;
import org.jivesoftware.openfire.plugin.jafka.service.OfflineMessageServiceImpl;
import org.jivesoftware.openfire.plugin.jafka.vo.UserNode;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class JafakaOfflineMsgListener implements OfflineMessageListener {
	private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Packet>> map = new ConcurrentHashMap<String, ConcurrentLinkedQueue<Packet>>();

	private UserNodeCache userNodeCache;
	private OfflineMessageService offlineMessageService;

	public JafakaOfflineMsgListener() {
		userNodeCache = RedisUserNodeCacheImpl.getInstance();
		offlineMessageService = OfflineMessageServiceImpl.getInstance();
	}

	public ConcurrentHashMap<String, ConcurrentLinkedQueue<Packet>> getPacketQueueMap() {
		return map;
	}

	private void enQueue(Packet packet) {
		JID fullJid = packet.getTo();
		List<UserNode> userNodes = userNodeCache.get(fullJid.getNode());
		if (CollectionUtils.isEmpty(userNodes)) {
			if (packet instanceof Message) {
				offlineMessageService.saveOfflineMsg((Message) packet);
			}
			return;
		}

		for (UserNode node : userNodes) {
			if (node.getResource().equalsIgnoreCase(fullJid.getResource())) {
				continue;
			}
			ConcurrentLinkedQueue<Packet> packetQueue = map.get(node.getNodeName());
			if (packetQueue == null) {
				packetQueue = new ConcurrentLinkedQueue<Packet>();
				map.put(node.getNodeName(), packetQueue);
			}
			packetQueue.add(packet);
		}
	}

	public void messageDeleted(Message message) {
		enQueue(message);
	}

	@Override
	public void messageBounced(Message message) {
		enQueue(message);
	}

	@Override
	public void messageStored(Message message) {
		enQueue(message);
	}

}
