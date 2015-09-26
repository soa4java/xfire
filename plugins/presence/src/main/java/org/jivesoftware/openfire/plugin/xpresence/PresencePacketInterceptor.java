package org.jivesoftware.openfire.plugin.xpresence;

import net.yanrc.web.xweb.presence.api.PresenceApi;
import net.yanrc.web.xweb.presence.domain.PresenceDTO;

import org.jivesoftware.of.common.spring.SpringContextHolder;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

public class PresencePacketInterceptor implements PacketInterceptor {

	PresenceApi presenceApi;

	public PresencePacketInterceptor() {
		presenceApi = SpringContextHolder.getBean("presenceApi", PresenceApi.class);
	}

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {
		if (packet instanceof Presence) {
			if (incoming && processed) {
				Presence p = (Presence) packet;
				JID jid = p.getFrom();
				Presence.Show show = p.getShow();
				String showStr = (show != null ? show.name() : "");
				PresenceDTO presence = new PresenceDTO(jid.getNode(), jid.getDomain(), "0", jid.getResource(),
						p.getStatus(), showStr, "wifi", System.currentTimeMillis());
				presenceApi.putPresence(presence);
			}
		}
	}
}
