package org.jivesoftware.openfire.plugin.xroster.internal.listener;

import org.jivesoftware.of.common.domain.UserTicket;
import org.jivesoftware.of.common.utils.SessionUtils;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

public abstract class UserTickets {
	
	public static  UserTicket newUserTicket(Presence p) {
		JID jid = p.getFrom();
		Presence.Show show = p.getShow();
		String personId = jid.getNode();
		String domain = jid.getDomain();
		String resource = jid.getResource();

		String status = p.getElement().elementText("status");
		String showStr = (show != null ? show.name() : "");
		String netstatus = p.getElement().elementText("netstatus");

		String nodeId = "";
		long timeStamp = System.currentTimeMillis();
		String tenantId = SessionUtils.getTopGroupId(p.getFrom());

		UserTicket userTicket = new UserTicket(personId, domain, resource, status, showStr, netstatus, nodeId,
				timeStamp, tenantId);
		return userTicket;
	}

}
