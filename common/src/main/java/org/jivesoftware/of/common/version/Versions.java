package org.jivesoftware.of.common.version;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;
import org.xmpp.packet.JID;

public class Versions {
	public static final String VERSION = "version";

	static final String MSG_RESENT_TARGET_VERSION = "message.receipt.target.version";
	static final String DEFAULT_TARGET_VERSION = "1.0.30";
	static String targetVersion = "1.0.30";

	static {
		targetVersion = JiveGlobals.getProperty(MSG_RESENT_TARGET_VERSION, DEFAULT_TARGET_VERSION);
		PropertyEventDispatcher.addListener(new PropertyListener());
	}

	public static String getCurrVersion(ClientSession clientSession) {
		if (clientSession instanceof LocalClientSession) {
			LocalClientSession localClientSession = (LocalClientSession) clientSession;
			Object versionObj = localClientSession.getSessionData(VERSION);
			String curVersion = null;
			if (versionObj != null) {
				curVersion = versionObj.toString();
			}
			return curVersion;
		}
		return null;
	}

	public static boolean currentVersionIsGreatThan(JID fromJid, String configedVersion) {
		if (fromJid != null) {
			ClientSession clientSession = XMPPServer.getInstance().getSessionManager().getSession(fromJid);
			if (clientSession instanceof LocalClientSession) {
				LocalClientSession localClientSession = (LocalClientSession) clientSession;
				Object versionObj = localClientSession.getSessionData(VERSION);
				String curVersion = null;
				if (versionObj != null) {
					curVersion = versionObj.toString();
				}
				return isGreatThan(curVersion, configedVersion);
			}
		}
		return false;
	}

	public static boolean isGreatThan(String curVersion, String otherVersion) {
		if (curVersion != null && (curVersion.startsWith("v") || curVersion.startsWith("V"))) {
			curVersion = curVersion.trim();
			curVersion = curVersion.substring(1);
		}

		if (otherVersion != null && (otherVersion.startsWith("v") || otherVersion.startsWith("V"))) {
			otherVersion = otherVersion.trim();
			otherVersion = otherVersion.substring(1);
		}

		if (StringUtils.isBlank(curVersion) && StringUtils.isBlank(otherVersion)) {
			return false;
		} else if (StringUtils.isBlank(curVersion) && StringUtils.isNotBlank(otherVersion)) {
			return false;
		} else if (StringUtils.isNotBlank(curVersion) && StringUtils.isBlank(otherVersion)) {
			return true;
		} else if (StringUtils.isNotBlank(curVersion) && StringUtils.isNotBlank(otherVersion)) {
			String[] curVerArr = curVersion.split("\\.");
			String[] otherVerArr = otherVersion.split("\\.");
			int minLen = Math.min(curVerArr.length, otherVerArr.length);
			for (int i = 0; i < minLen; i++) {
				long j = (StringUtils.isNotEmpty(curVerArr[i]) && StringUtils.isNumeric(curVerArr[i])) ? Long
						.parseLong(curVerArr[i]) : 0L;
				long k = (StringUtils.isNotEmpty(otherVerArr[i]) && StringUtils.isNumeric(otherVerArr[i])) ? Long
						.parseLong(otherVerArr[i]) : 0L;
				if (j - k > 0) {
					return true;
				} else if (j - k == 0) {
					continue;
				} else {
					return false;
				}
			}

			if (curVerArr.length > otherVerArr.length) {
				return true;
			}

			return false;
		}

		return false;
	}

	private static class PropertyListener implements PropertyEventListener {
		public void propertySet(String property, Map<String, Object> params) {
			if (MSG_RESENT_TARGET_VERSION.equals(property)) {
				String value = (String) params.get("value");
				if (value != null) {
					targetVersion = value;
				}
			}
		}

		public void propertyDeleted(String property, Map<String, Object> params) {
			if (MSG_RESENT_TARGET_VERSION.equals(property)) {
				targetVersion = DEFAULT_TARGET_VERSION;
			}
		}

		public void xmlPropertySet(String property, Map<String, Object> params) {
			// Do nothing
		}

		public void xmlPropertyDeleted(String property, Map<String, Object> params) {
			// Do nothing
		}
	}

}
