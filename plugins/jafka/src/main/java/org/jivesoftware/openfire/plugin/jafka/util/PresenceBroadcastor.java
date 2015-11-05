package org.jivesoftware.openfire.plugin.jafka.util;

import java.util.List;
import java.util.Map;

import net.yanrc.app.common.util.JsonUtils;
import net.yanrc.web.xweb.presence.api.PresenceSubscriptionApi;
import net.yanrc.web.xweb.presence.enums.SubscriptionRelationStatus;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jivesoftware.of.common.enums.Resource;
import org.jivesoftware.of.common.spring.SpringContextHolder;
import org.jivesoftware.of.common.utils.SessionUtils;
import org.jivesoftware.openfire.RoutingTable;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.util.JiveProperties;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

public abstract class PresenceBroadcastor {
	static final Logger LOG = LoggerFactory.getLogger(PresenceBroadcastor.class);
	static RoutingTable routingTable = XMPPServer.getInstance().getRoutingTable();
	static String xmppDomain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
	static String VCARD = "vcard";
	static String HASH = "hash";
	static String BROADCAST_ENABLE = "presence.broadcast.enable";
	public static boolean presenceBroadcastEnable;
	static PresenceSubscriptionApi presenceSubscriptionApi;

	static {
		presenceSubscriptionApi = SpringContextHolder.getBean(PresenceSubscriptionApi.class);
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
			LOG.warn("invalid presence: {}", original.toXML());
			return;
		}

		String personId = from.getNode();
		String resourceCode = from.getResource();
		String tenantId = SessionUtils.getTopGroupId(from);
		List<String> subscribersLifecycleJsonStrings = presenceSubscriptionApi.getSubscribersLifecycleJsonStrings(
				tenantId, personId, resourceCode).getModel();

		if (CollectionUtils.isEmpty(subscribersLifecycleJsonStrings)) {
			LOG.warn("reqId:{},无法获取到租户:{} 下的presence订阅用户信息", original.getID());
			return;
		}

		//String fromFullJID = presence.getFrom().toFullJID();
		for (String status : subscribersLifecycleJsonStrings) {
			if (status == null) {
				LOG.warn("status is null, personId: {}", personId);
				continue;
			}

			SubRelationLifecycle subStatus = JsonUtils.toBean(status, SubRelationLifecycle.class);

			//订阅着为非激活不广播{"status":"0","inactivityTime":-1,"terminal":{"personId":"893036efa9db932e55683ff925fb5bc1","resource":"ERC"}}
			if (!StringUtils.equalsIgnoreCase(subStatus.getStatus(), SubscriptionRelationStatus.AVAILABLE.getCode())) {
				continue;
			}

			//本人不广播
			if (subStatus.getTerminal() == null || personId.equals(subStatus.getTerminal().getPersonId())) {
				continue;
			}

			String subscriberResourceCode = subStatus.getTerminal().getResource();
			//只广播给PC端，非PC端不广播
			if (!StringUtils.equalsIgnoreCase(subscriberResourceCode, Resource.PC.getCode())) {
				continue;
			}

			Presence copy = presence.createCopy();
			JID jid = new JID(subStatus.getTerminal().getPersonId(), xmppDomain, subscriberResourceCode);
			copy.setTo(jid);
			ClientSession clientSession = SessionManager.getInstance().getSession(jid);
			if (clientSession != null) {
				clientSession.deliverRawText(copy.toXML());
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
