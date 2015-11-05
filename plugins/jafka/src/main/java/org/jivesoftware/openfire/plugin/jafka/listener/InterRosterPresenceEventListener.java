package org.jivesoftware.openfire.plugin.jafka.listener;

import java.util.Collection;

import net.yanrc.web.xweb.presence.api.PresenceSubscriptionApi;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.of.common.domain.UserTicket;
import org.jivesoftware.of.common.enums.Resource;
import org.jivesoftware.of.common.spring.SpringContextHolder;
import org.jivesoftware.of.common.utils.SessionUtils;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.plugin.jafka.JafkaPlugin;
import org.jivesoftware.openfire.plugin.jafka.cache.UserNodeCache;
import org.jivesoftware.openfire.plugin.jafka.cache.impl.redis.RedisUserNodeCacheImpl;
import org.jivesoftware.openfire.plugin.jafka.util.PresenceBroadcasts;
import org.jivesoftware.openfire.plugin.jafka.util.UserTickets;
import org.jivesoftware.openfire.plugin.jafka.vo.UserNode;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.PresenceEventListener;
import org.jivesoftware.util.JiveProperties;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

public class InterRosterPresenceEventListener implements PresenceEventListener {

	PresenceSubscriptionApi presenceSubscriptionApi;
	UserNodeCache userNodeCache;

	public InterRosterPresenceEventListener() {
		presenceSubscriptionApi = SpringContextHolder.getBean(PresenceSubscriptionApi.class);
		userNodeCache = RedisUserNodeCacheImpl.getInstance();
	}

	@Override
	public void availableSession(ClientSession session, Presence presence) {

		JID jid = presence.getFrom();
		UserNode userNode = new UserNode(jid.getNode(), jid.getDomain(), jid.getResource(), JafkaPlugin.nodeName);
		userNodeCache.put(jid.getNode(), userNode);

		session.setMessageCarbonsEnabled(true);

		avaiableSubscriptionRelation(true, session, presence);
	}

	@Override
	public void unavailableSession(ClientSession session, Presence presence) {

		JID jid = presence.getFrom();
		UserNode userNode = new UserNode(jid.getNode(), jid.getDomain(), jid.getResource(), JafkaPlugin.nodeName);
		userNodeCache.remove(jid.getNode(), userNode);

		avaiableSubscriptionRelation(false, session, presence);
	}

	@Override
	public void presenceChanged(ClientSession session, Presence presence) {
		if (presence == null || JiveProperties.getInstance().getBooleanProperty("xmpp.auth.anonymous", false)) {
			return;
		}

		doPresenceBroadcastIfNecessary(presence, session);
	}

	private void avaiableSubscriptionRelation(boolean actived, ClientSession session, Presence presence) {
		if (presence.getFrom() == null) {
			return;
		}
		String tenantId = SessionUtils.getTopGroupId(presence.getFrom());
		String personId = presence.getFrom().getNode();
		String resource = presence.getFrom().getResource();

		UserTicket userTicket = UserTickets.newUserTicket(presence);
		userTicket.setNodeName(JafkaPlugin.nodeName);

		if (actived) {
			presenceSubscriptionApi.activeSubscriptionRelationThenPublishUserTicket(tenantId, personId, resource,
					userTicket);
		} else {
			presenceSubscriptionApi.inactiveSubscriptionRelationThenPublishUserTicket(tenantId, personId, resource,
					userTicket);
		}
		PresenceBroadcasts.broadCastPresence(presence.createCopy(), presence);
	}

	private void doPresenceBroadcastIfNecessary(Presence presence, ClientSession session) {

		JID from = presence.getFrom();
		if (from == null) {
			return;
		}

		JID recipientJID = presence.getTo();
		Presence copy = presence.createCopy();

		if (session != null) {
			if (Resource.isMobile(presence.getFrom().getResource())) {
				Collection<ClientSession> sessions = SessionManager.getInstance().getSessions(
						presence.getFrom().getNode());
				for (ClientSession sess : sessions) {
					if (!StringUtils
							.equalsIgnoreCase(sess.getAddress().getResource(), presence.getFrom().getResource())) {
						sess.deliverRawText(presence.toXML());
					}
				}
			}

			//非跨域过来的,成功登录的，缓存域信息，并将presence广播给sender所在集团的在该域的所有在线用户
			if (session.getStatus() == Session.STATUS_AUTHENTICATED || session.getStatus() == Session.STATUS_CLOSED) {
				if (PresenceBroadcasts.needBroadcast(presence)) {
					PresenceBroadcasts.broadCastPresence(copy, presence);
				}
			}
			//更新跨域状态
			UserTicket userTicket= UserTickets.newUserTicket(presence);
			userTicket.setNodeName(JafkaPlugin.nodeName);
			presenceSubscriptionApi.publishUserTicket(userTicket);
			return;
		}

		if (recipientJID == null) {
			return;
		}

		//如果是跨域过来的,将presence广播给sender所在集团的在该域的所有在线用户
		if (!StringUtils.equalsIgnoreCase(recipientJID.getDomain(), from.getDomain())) {
			if (PresenceBroadcasts.needBroadcast(presence)) {
				PresenceBroadcasts.broadCastPresence(copy, presence);
			}
			if (null == presence.getExtension(XConstants.NO_ROUTE, XConstants.NO_ROUTE_HINTS)) {
				presence.addChildElement(XConstants.NO_ROUTE, XConstants.NO_ROUTE_HINTS);//标志该消息不需要再路由
			}
		}
		//更新跨域状态
		presenceSubscriptionApi.publishUserTicket(UserTickets.newUserTicket(presence));
	}

	@Override
	public void subscribedToPresence(JID subscriberJID, JID authorizerJID) {

	}

	@Override
	public void unsubscribedToPresence(JID unsubscriberJID, JID recipientJID) {
	}

}
