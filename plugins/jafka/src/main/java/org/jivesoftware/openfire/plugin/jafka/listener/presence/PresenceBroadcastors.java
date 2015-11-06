package org.jivesoftware.openfire.plugin.jafka.listener.presence;

import java.util.List;
import java.util.Map;

import net.yanrc.web.xweb.presence.domain.SubscriptionRelationLifecycle;
import net.yanrc.web.xweb.presence.enums.SubscriptionRelationStatus;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jivesoftware.of.common.enums.Resource;
import org.jivesoftware.of.common.utils.SessionUtils;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.plugin.jafka.ClusterPlugin;
import org.jivesoftware.openfire.plugin.jafka.ClusterRemoteApis;
import org.jivesoftware.openfire.plugin.jafka.vo.PacketQueue;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.util.JiveProperties;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

public class PresenceBroadcastors {
	private static Logger logger = LoggerFactory.getLogger(PresenceBroadcastors.class);
	private static String xmppDomain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
	private static String BROADCAST_ENABLE = "presence.broadcast.enable";

	public static boolean presenceBroadcastEnable;

	static {
		presenceBroadcastEnable = JiveProperties.getInstance().getBooleanProperty(BROADCAST_ENABLE, true);
		PropertyEventDispatcher.addListener(new PropertyListener());
	}

	/**
	 * 广播给所有在线用户
	 *
	 * @param original
	 * @param presence
	 */
	public static void broadcastPresence(Presence original, Presence presence) {

		if (!presenceBroadcastEnable) {
			return;
		}

		JID from = original.getFrom();

		if (from == null || StringUtils.isBlank(from.getNode())) {
			logger.warn("invalid presence: {}", original.toXML());
			return;
		}

		String personId = from.getNode();
		String resourceCode = from.getResource();
		String tenantId = SessionUtils.getTopGroupId(from);
		List<SubscriptionRelationLifecycle> subscriptionRelationLifecycles = ClusterRemoteApis
				.getSubscribersLifecycles(personId, resourceCode, tenantId);

		if (CollectionUtils.isEmpty(subscriptionRelationLifecycles)) {
			logger.warn("reqId:{},无法获取到租户:{} 下的presence订阅用户信息", original.getID());
			return;
		}

		//String fromFullJID = presence.getFrom().toFullJID();
		for (SubscriptionRelationLifecycle subStatus : subscriptionRelationLifecycles) {
			if (subStatus == null) {
				logger.warn("status is null, personId: {}", personId);
				continue;
			}


			//订阅着为非激活不广播{"status":"0","inactivityTime":-1,"terminal":{"personId":"893036efa9db932e55683ff925fb5bc1","resource":"ERC"}}
			if (StringUtils.isBlank(subStatus.getNodeName())
					|| !StringUtils.equalsIgnoreCase(subStatus.getStatus().getCode(),
							SubscriptionRelationStatus.AVAILABLE.getCode())) {
				continue;
			}

			//本人不广播
			if (subStatus.getTerminal() == null || personId.equals(subStatus.getTerminal().getPersonId())) {
				continue;
			}

			String subscriberResourceCode = subStatus.getTerminal().getResource().getCode();
			//只广播给PC端，非PC端不广播
			if (!StringUtils.equalsIgnoreCase(subscriberResourceCode, Resource.PC.getCode())) {
				continue;
			}

			Presence copy = presence.createCopy();
			JID jid = new JID(subStatus.getTerminal().getPersonId(), xmppDomain, subscriberResourceCode);
			copy.setTo(jid);

			if (StringUtils.endsWithIgnoreCase(ClusterPlugin.nodeName, subStatus.getNodeName())) {
				ClientSession clientSession = SessionManager.getInstance().getSession(jid);
				if (clientSession != null) {
					clientSession.deliverRawText(copy.toXML());
				}
			} else {
				PacketQueue.getInstance().add(subStatus.getNodeName(), copy);
			}
		}
	}

	private static class PropertyListener implements PropertyEventListener {
		public void propertySet(String property, Map<String, Object> params) {
			if (BROADCAST_ENABLE.equals(property)) {
				String value = (String) params.get("value");
				if (value != null) {
					presenceBroadcastEnable = Boolean.parseBoolean(value);
				}
			}
		}

		public void propertyDeleted(String property, Map<String, Object> params) {
			if (BROADCAST_ENABLE.equals(property)) {
				presenceBroadcastEnable = true;
			}
		}

		public void xmlPropertySet(String property, Map<String, Object> params) {
			// Do nothing
		}

		public void xmlPropertyDeleted(String property, Map<String, Object> params) {
			// Do nothing
		}
	}

}
