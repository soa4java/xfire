package org.jivesoftware.openfire.plugin.jafka.listener;

import org.jivesoftware.openfire.plugin.jafka.JafkaPlugin;
import org.jivesoftware.openfire.plugin.jafka.cache.UserNodeCache;
import org.jivesoftware.openfire.plugin.jafka.cache.impl.redis.RedisUserNodeCacheImpl;
import org.jivesoftware.openfire.plugin.jafka.vo.UserNode;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.user.PresenceEventListener;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

public class JafkaPresenceEventListener implements PresenceEventListener {

	private UserNodeCache userNodeCache;

	public JafkaPresenceEventListener() {
		userNodeCache = RedisUserNodeCacheImpl.getInstance();
	}

	@Override
	public void availableSession(ClientSession session, Presence presence) {
		JID jid = presence.getFrom();
		UserNode userNode = new UserNode(jid.getNode(), jid.getDomain(), jid.getResource(), JafkaPlugin.nodeName);
		userNodeCache.put(jid.getNode(), userNode);
	}

	@Override
	public void unavailableSession(ClientSession session, Presence presence) {
		JID jid = presence.getFrom();
		UserNode userNode = new UserNode(jid.getNode(), jid.getDomain(), jid.getResource(), JafkaPlugin.nodeName);
		userNodeCache.remove(jid.getNode(), userNode);
	}

	@Override
	public void presenceChanged(ClientSession session, Presence presence) {
	}

	@Override
	public void subscribedToPresence(JID subscriberJID, JID authorizerJID) {

	}

	@Override
	public void unsubscribedToPresence(JID unsubscriberJID, JID recipientJID) {
	}

}
