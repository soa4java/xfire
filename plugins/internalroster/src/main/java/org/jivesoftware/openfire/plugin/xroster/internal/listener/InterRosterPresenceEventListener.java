package org.jivesoftware.openfire.plugin.xroster.internal.listener;

import java.util.Collection;

import net.yanrc.web.xweb.presence.api.PresenceSubscriptionApi;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.of.common.enums.Resource;
import org.jivesoftware.of.common.spring.SpringContextHolder;
import org.jivesoftware.of.common.utils.SessionUtils;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.PresenceEventListener;
import org.jivesoftware.util.JiveProperties;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

public class InterRosterPresenceEventListener implements PresenceEventListener {

	PresenceSubscriptionApi presenceSubscriptionApi;

	public InterRosterPresenceEventListener() {
		presenceSubscriptionApi = SpringContextHolder.getBean(PresenceSubscriptionApi.class);
	}

	@Override
	public void availableSession(ClientSession session, final Presence presence) {
		//激活订阅关系
		if (presence.getFrom() != null) {
			String tenantId = SessionUtils.getTopGroupId(presence.getFrom());
			String personId = presence.getFrom().getNode();
			String resource = presence.getFrom().getResource();
			presenceSubscriptionApi.activeSubscriptionRelation(tenantId, personId, resource);
		}
	}
	
	@Override
	public void unavailableSession(ClientSession session, Presence presence) {
		if (presence.getFrom() != null) {
			presenceSubscriptionApi.publishUserTicket(UserTickets.newUserTicket(presence));
		}
	}

	@Override
	public void presenceChanged(ClientSession session, Presence presence) {
		if (presence == null || JiveProperties.getInstance().getBooleanProperty("xmpp.auth.anonymous", false)) {
			return;
		}

		JID recipientJID = presence.getTo();
		JID from = presence.getFrom();
		if (from == null) {
			return;
		}

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
			presenceSubscriptionApi.publishUserTicket(UserTickets.newUserTicket(presence));
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
