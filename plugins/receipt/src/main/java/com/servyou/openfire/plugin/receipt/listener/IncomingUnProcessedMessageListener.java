package com.servyou.openfire.plugin.receipt.listener;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.of.common.message.MessageListener;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Message;

import com.servyou.openfire.plugin.receipt.msgs.MessageClearer;
import com.servyou.openfire.plugin.receipt.msgs.ReceiptMsgUtils;

public class IncomingUnProcessedMessageListener implements MessageListener {

	static Logger logger = LoggerFactory.getLogger(IncomingProcessedMessageListener.class);

	static XMPPServer xmppServer = XMPPServer.getInstance();

	@Override
	public void process(Message msg, Session session, boolean incoming, boolean processed) {
		if (incoming && !processed) {
			if (!(session instanceof ClientSession)) {
				return;
			}

			if (!(session instanceof ClientSession)) {
				return;
			}

			if (!ReceiptMsgUtils.isNeedReceipt(msg) || ReceiptMsgUtils.isOfflineMsg(msg) || !xmppServer.isLocal(msg.getFrom())) {
				return;
			}
			String msgId = ReceiptMsgUtils.getMsgIdFromClientReceiptMsg(msg);
			if (StringUtils.isNotBlank(msgId)) {
				MessageClearer.remove(msgId, msg.getTo());
			}
		}
	}

}
