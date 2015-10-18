package org.jivesoftware.of.common.message;

import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Presence;

public interface PresenceListener {

	public void process(Presence Presence, Session session, boolean incoming, boolean processed);

}
