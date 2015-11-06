package org.jivesoftware.of.common.message;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.collections.CollectionUtils;
import org.jivesoftware.of.common.message.offline.OfflineMessageManager;
import org.jivesoftware.of.common.message.offline.OfflineMessageService;
import org.jivesoftware.of.common.node.UserNode;
import org.jivesoftware.of.common.node.cache.UserNodeCache;
import org.jivesoftware.of.common.node.cache.impl.redis.RedisUserNodeCacheImpl;
import org.jivesoftware.util.JiveGlobals;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class PacketQueue {

	public static String nodeName;
	public static String nodeIp;

	static {
		nodeName = JiveGlobals.getXMLProperty("imserver.node.name");
		nodeIp = JiveGlobals.getXMLProperty("imserver.node.ip");
	}

	private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Packet>> map = new ConcurrentHashMap<String, ConcurrentLinkedQueue<Packet>>();

	private UserNodeCache userNodeCache;
	private OfflineMessageService offlineMessageService;

	private static PacketQueue instance;

	public static PacketQueue getInstance() {
		if (instance == null) {
			synchronized (PacketQueue.class) {
				if (instance == null) {
					instance = new PacketQueue();
				}
			}
		}

		return instance;
	}

	private PacketQueue() {
		userNodeCache = RedisUserNodeCacheImpl.getInstance();
		offlineMessageService = OfflineMessageManager.getInstance().getOfflineMessageService();
	}

	public void enQueue(Packet packet) {
		JID fullJid = packet.getTo();
		List<UserNode> userNodes = userNodeCache.get(fullJid.getNode());
		if (CollectionUtils.isEmpty(userNodes)) {
			if (packet instanceof Message) {
				offlineMessageService.saveOfflineMsg((Message) packet);
			}
			return;
		}

		for (UserNode node : userNodes) {
			if (node.getNodeName().equalsIgnoreCase(nodeName)) {//在本地节点已经发送过了
				continue;
			}

			add(node.getNodeName(), packet);

		}
	}

	public void add(String nodeName, Packet packet) {
		ConcurrentLinkedQueue<Packet> packetQueue = map.get(nodeName);
		if (packetQueue == null) {
			packetQueue = new ConcurrentLinkedQueue<Packet>();
			map.put(nodeName, packetQueue);
		}
		packetQueue.add(packet);
	}

	public ConcurrentHashMap<String, ConcurrentLinkedQueue<Packet>> getPacketQueueMap() {
		return map;
	}

}
