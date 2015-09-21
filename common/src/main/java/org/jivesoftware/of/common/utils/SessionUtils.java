package org.jivesoftware.of.common.utils;

import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

public class SessionUtils {
	
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
    public static boolean isExceptionalltUnavailable(ClientSession session, Presence presence){
        JID recipientJID = presence.getTo();
        if (recipientJID == null && session.getStatus() == Session.STATUS_CLOSED) {//服务端发出的
            return true;
        }

        return false;
    }

}
