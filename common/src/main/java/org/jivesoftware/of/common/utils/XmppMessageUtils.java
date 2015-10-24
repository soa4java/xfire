package org.jivesoftware.of.common.utils;

import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

public abstract class XmppMessageUtils {

	public static Message createNormalMsg(JID from, JID to, boolean withXExt, String xmlns,
			Map<String, String> eleAndTextMap) {
		return createMsg(from.toString(), to.toString(), Message.Type.normal, withXExt, xmlns, eleAndTextMap);
	}

	public static Message createChatMsg(JID from, JID to, boolean withXExt, String xmlns,
			Map<String, String> eleAndTextMap) {
		return createMsg(from.toString(), to.toString(), Message.Type.chat, withXExt, xmlns, eleAndTextMap);
	}

	public static Message createMsg(String from, String to, org.xmpp.packet.Message.Type type, boolean withXExt,
			String xmlns, Map<String, String> eleAndTextMap) {
		Message msg = new Message();
		if (null != from) {
			msg.setFrom(from);
		}

		msg.setTo(to);
		msg.setType(type);
		Element root = msg.getElement();

		if (withXExt) {
			root = root.addElement("x", xmlns);
		}

		if (eleAndTextMap != null && !eleAndTextMap.isEmpty()) {
			Set<String> set = eleAndTextMap.keySet();
			for (String key : set) {
				root.addElement(key).setText(eleAndTextMap.get(key));
			}
		}
		return msg;
	}

}
