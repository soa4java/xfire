package org.jivesoftware.of.common.utils;

import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

public class SessionUtils {

	public static String getTopGroupId(JID from) {
		ClientSession session = SessionManager.getInstance().getSession(from);
		String topGroupId = null;
		if (session != null && session instanceof LocalClientSession) {
			LocalClientSession localSession = (LocalClientSession) session;
			topGroupId = (String) (localSession.getSessionData(XConstants.TOP_GROUP_ID));
		}

		return topGroupId;
	}

	public static void setTopGroupId(Session session, String topGroupId) {
		if (session != null && session instanceof LocalClientSession) {
			LocalClientSession sess = (LocalClientSession) session;
			sess.setSessionData(XConstants.TOP_GROUP_ID, topGroupId);
		}
	}

	public static String getToken(JID from) {
		ClientSession session = SessionManager.getInstance().getSession(from);
		String topGroupId = null;
		if (session != null && session instanceof LocalClientSession) {
			topGroupId = (String) (((LocalClientSession) session).getSessionData(XConstants.TOKEN));
		}
		return topGroupId;
	}

	/**
	 * 正常下线
	 * @param session
	 * @param presence
	 * @return
	 */
	public static boolean isNormallyUnavailable(ClientSession session, Presence presence) {
		if (!presence.isAvailable() && presence.getTo() == null && null == presence.getElement().attribute("final")) {
			return true;
		}

		return false;
	}

	/**
	 * 异常下线
	 * @param session
	 * @param presence
	 * @return
	 */
	public static boolean isExceptionalltUnavailable(ClientSession session, Presence presence) {
		JID recipientJID = presence.getTo();
		if (recipientJID == null && session.getStatus() == Session.STATUS_CLOSED) {//服务端发出的
			return true;
		}

		return false;
	}

}
