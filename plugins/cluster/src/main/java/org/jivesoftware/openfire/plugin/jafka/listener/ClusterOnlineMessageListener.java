package org.jivesoftware.openfire.plugin.jafka.listener;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jivesoftware.ext.listener.OnlineMessageListener;
import org.jivesoftware.of.common.message.PacketQueue;
import org.jivesoftware.of.common.node.ImNodes;
import org.jivesoftware.of.common.node.UserNode;
import org.jivesoftware.of.common.node.cache.UserNodeCache;
import org.jivesoftware.of.common.node.cache.impl.redis.RedisUserNodeCacheImpl;
import org.jivesoftware.of.common.thread.XExecutor;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

public class ClusterOnlineMessageListener implements OnlineMessageListener {

	static UserNodeCache userNodeCache = RedisUserNodeCacheImpl.getInstance();

	@Override
	public void onSucceed(final Message message) {
		XExecutor.messageCenterExecutor.submit(new Runnable() {

			@Override
			public void run() {
				List<UserNode> userNodes = userNodeCache.get(message.getTo().getNode());
				if (CollectionUtils.isEmpty(userNodes)) {
					return;
				}

				for (UserNode userNode : userNodes) {
					if (userNode != null && StringUtils.isNotBlank(userNode.getNodeName())
							&& !ImNodes.nodeName.equalsIgnoreCase(userNode.getNodeName())) {
						message.getElement().addAttribute("mc", "0");
						JID to = message.getTo();
						JID fullToJID = new JID(to.getNode(),to.getDomain(),userNode.getResource());
						message.setTo(fullToJID);
						PacketQueue.getInstance().add(userNode.getNodeName(), message);
					}
				}
			}
		});
	}
}
