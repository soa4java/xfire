package org.jivesoftware.openfire.plugin.xroster.receipt.intercept;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.of.common.message.Messages;
import org.jivesoftware.of.common.utils.SessionUtils;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.plugin.xroster.receipt.msgs.MessageClearer;
import org.jivesoftware.openfire.plugin.xroster.receipt.msgs.ReceiptMsgUtils;
import org.jivesoftware.openfire.plugin.xroster.receipt.msgs.ReceiptVersions;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class IncomingUnProcessedMessageListener implements PacketInterceptor {

	static Logger logger = LoggerFactory.getLogger(IncomingProcessedMessageListener.class);

	static XMPPServer xmppServer = XMPPServer.getInstance();

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {
		if (incoming && !processed) {

			if (!(packet instanceof Message)) {
				return;
			}

			Message msg = (Message) packet;

			//根据发送方的版本判断，是否需要给发送方回执
			if ((SessionUtils.receiptEnabled(session) || (xmppServer.isLocal(msg.getFrom()) && ReceiptVersions.versionMatch(msg.getFrom())))
					&& !(Messages.isControlMessage(msg))) {
				ReceiptVersions.sendSrvReceipt(msg, session);
			}

			if (!(session instanceof ClientSession)) {
				return;
			}

			//			if (!ReceiptMsgUtils.isNeedReceipt(msg) || ReceiptMsgUtils.isOfflineMsg(msg) || !xmppServer.isLocal(msg.getFrom())) {
			if (ReceiptMsgUtils.isOfflineMsg(msg)) {
				return;
			}

			String msgId = ReceiptMsgUtils.getMsgIdFromClientReceiptMsg(msg);
			if (StringUtils.isNotBlank(msgId)) {
				MessageClearer.remove(msgId, msg.getFrom());
			}
		}
	}

}
