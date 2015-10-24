package org.jivesoftware.openfire.plugin.xpresence;

import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Packet;

public class PresencePacketInterceptor implements PacketInterceptor {

//	UserTicketApi userTicketApi;
//
//	public PresencePacketInterceptor() {
//		userTicketApi = SpringContextHolder.getBean("userTicketApi", UserTicketApi.class);
//	}

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {
//		if (packet instanceof Presence) {
//			if (incoming && processed) {
//				Presence p = (Presence) packet;
//				JID jid = p.getFrom();
//				Presence.Show show = p.getShow();
//				String personId = jid.getNode();
//				String domain = jid.getDomain();
//				String resource = jid.getResource();
//
//				String status = p.getElement().elementText("status");
//				String showStr = (show != null ? show.name() : "");
//				String netstatus = p.getElement().elementText("netstatus");
//
//				String nodeId = "";
//				long timeStamp = System.currentTimeMillis();
//				String tenantId = SessionUtils.getTopGroupId(packet.getFrom());
//
//				UserTicket userTicket = new UserTicket(personId, domain, resource, status, showStr, netstatus, nodeId,
//						timeStamp, tenantId);
//
//				userTicketApi.publishUserTicket(userTicket);
//			}
//		}
	}
}
