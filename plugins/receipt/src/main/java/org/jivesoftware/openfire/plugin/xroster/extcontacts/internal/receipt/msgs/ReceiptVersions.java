package org.jivesoftware.openfire.plugin.xroster.extcontacts.internal.receipt.msgs;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.of.common.enums.Resource;
import org.jivesoftware.of.common.version.Versions;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

public abstract class ReceiptVersions {
	

	public static  boolean versionMatch(JID fullJID) {
		String resource = fullJID.getResource();
		if (StringUtils.isBlank(resource)) {
			return false;
		}

		if (StringUtils.equals(Resource.PC.getCode(), resource)) {
			return Versions.currentVersionIsGreatThan(fullJID, ReceiptVersion.pcVersion);
		}

		return Versions.currentVersionIsGreatThan(fullJID, ReceiptVersion.appVersion);
	}

	public static  void  sendSrvReceipt(Message msg, Session session) {
		Message receiptMessage = ReceiptMsgUtils.createMsgReceipt(msg);
		session.deliverRawText(receiptMessage.toXML());
	}

}
