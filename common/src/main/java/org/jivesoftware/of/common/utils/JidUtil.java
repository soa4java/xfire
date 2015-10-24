/**
 *
 */
package org.jivesoftware.of.common.utils;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.of.common.enums.Resource;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.JiveProperties;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

/**
 * jid转换的工具类
 */
public class JidUtil {

	public static String DOMAIN = JiveGlobals.getProperty("xmpp.domain", "127.0.0.1").toLowerCase();

	public static String DOMAIN_TAG = "@" + JidUtil.DOMAIN;

	public static String getDomain(JID jid) {
		if (jid != null && StringUtils.isNotBlank(jid.getDomain())) {
			String domain = jid.getDomain();
			String[] arr = domain.split("\\.");
			return arr[arr.length - 1];
		}
		return "";
	}

	/**
	 * 判断jidstr是否能转化成bareJid
	 *
	 * @param jidStr
	 * @return
	 */
	public static boolean canTransformToBareJid(String jidStr) {
		JID jid = new JID(jidStr);
		boolean isCrossDomain = JiveProperties.getInstance().getBooleanProperty("is.crossDomain.repeat", true);
		if (isCrossDomain) {
			if (StringUtils.isBlank(jid.getNode()) || StringUtils.isBlank(jid.getDomain())) {
				return false;
			}
		} else {
			if (StringUtils.isBlank(jid.getNode()) || !(jid.asBareJID().toString().endsWith(DOMAIN_TAG))) {
				return false;
			}
		}

		return true;
	}

	//    public static String nodeToFullJid(String id) {
	//        return id + "@" + DOMAIN + "/" + SessionUtils.getPresenceResource(id);
	//    }

	public static String nodeToPcJid(String id) {
		return id + "@" + DOMAIN + "/" + Resource.PC.getCode();
	}

	/**
	 * 把节点名转换成jid
	 *
	 * @param id
	 * @return
	 */

	public static String nodeToBareJid(String id) {
		return id + "@" + DOMAIN;
	}

	/**
	 * 把jid转换成id 格式必须为 xxx@xxx id@domain 否则直接返回原串
	 *
	 * @param jid
	 * @return
	 */
	public static String jidToNode(String jid) {
		if (jid == null || jid.trim().equals("") || !jid.contains("@")) {
			return jid;
		}
		return jid.split("@")[0];
	}

	public static boolean nodeEqual(Packet packet) {
		return (packet != null && packet.getFrom() != null && packet.getTo() != null && StringUtils.equals(packet
				.getFrom().getNode(), packet.getTo().getNode()));
	}

}
