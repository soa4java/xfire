package org.jivesoftware.openfire.plugin.jafka.interceptor;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jivesoftware.of.common.enums.ImPotocal;
import org.jivesoftware.of.common.message.PacketQueue;
import org.jivesoftware.of.common.node.ImNodes;
import org.jivesoftware.of.common.node.UserNode;
import org.jivesoftware.of.common.node.cache.UserNodeCache;
import org.jivesoftware.of.common.node.cache.impl.redis.RedisUserNodeCacheImpl;
import org.jivesoftware.of.common.thread.XExecutor;
import org.jivesoftware.of.common.utils.ImPotocals;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.plugin.jafka.ClusterMessages;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketExtension;

public class MessageTimestampPacketInterceptor implements PacketInterceptor {

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {
		if (packet instanceof Message) {
			if (incoming && !processed) {
				final Message message = (Message) packet;
				if (Message.Type.chat == message.getType() || Message.Type.groupchat == message.getType()) {
					Timetamps.addTimetamp(message);
				}
				

				if (ImPotocals.isReceipt(message)) {
					return;
				}

				if (message.getElement().attribute("mc") != null) {
					return;
				}
				//转发给自己的其他终端
//				XExecutor.globalExecutor.submit(new Runnable() {
//
//					@Override
//					public void run() {
//						send(message, message.getFrom(), true);
//					}
//				});
			}
		}

	}

	static UserNodeCache userNodeCache = RedisUserNodeCacheImpl.getInstance();
	
	public static void send(Message message, JID recipientJID, boolean toSelf) {
		List<UserNode> userNodes = userNodeCache.get(recipientJID.getNode());
		if (CollectionUtils.isEmpty(userNodes)) {
			return;
		}

		for (UserNode userNode : userNodes) {
			if (userNode != null && StringUtils.isNotBlank(userNode.getNodeName())
					&& !ImNodes.nodeName.equalsIgnoreCase(userNode.getNodeName())) {
				message.getElement().addAttribute("mc", "0");
				JID to = recipientJID;
				JID fullToJID = new JID(to.getNode(), to.getDomain(), userNode.getResource());
				message.setTo(fullToJID);

				if (toSelf) {
					if (StringUtils.endsWithIgnoreCase(userNode.getResource(), recipientJID.getResource())) {
						continue;
					}
					message.addExtension(new PacketExtension(ImPotocal.SynToSelf.extName(), ImPotocal.SynToSelf
							.extNamspace()));
					Element alias = message.getElement().addElement("alias");
					alias.addAttribute("to", recipientJID.toBareJID());
				}

				PacketQueue.getInstance().add(userNode.getNodeName(), message);
			}
		}
	}
}
