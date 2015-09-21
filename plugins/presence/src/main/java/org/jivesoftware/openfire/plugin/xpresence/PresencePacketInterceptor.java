package org.jivesoftware.openfire.plugin.xpresence;

import net.yanrc.web.xweb.presence.service.PresenceService;

import org.jivesoftware.of.common.spring.SpringContextHolder;
import org.jivesoftware.of.common.status.Status;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

public class PresencePacketInterceptor implements PacketInterceptor {

	PresenceService presenceService;

	public PresencePacketInterceptor() {
		presenceService = SpringContextHolder.getBean("presenceService", PresenceService.class);
	}

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {

		if (incoming && processed && packet instanceof Presence) {
			Presence p = (Presence) packet;
			JID jid = p.getFrom();
			Presence.Show show = p.getShow();
			String showStr = (show != null ? show.name() : "");
			Status status = new Status(jid.getNode(), jid.getDomain(), jid.getResource(), p.getStatus(), showStr,
					p.getNetstatus(), p.getPriority(), System.currentTimeMillis());
			presenceService.setPresence(status);
		}

	}
}
