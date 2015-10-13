package com.servyou.openfire.plugin.receipt.listener;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.of.common.enums.JidResourceEnum;
import org.jivesoftware.of.common.message.MessageListener;
import org.jivesoftware.of.common.version.Versions;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

import com.servyou.openfire.plugin.receipt.ReceiptsProps;
import com.servyou.openfire.plugin.receipt.msgs.MessageQueueMap;
import com.servyou.openfire.plugin.receipt.msgs.MessageQueueWaitingReceipt;
import com.servyou.openfire.plugin.receipt.msgs.ReceiptMsgUtils;
import com.servyou.openfire.plugin.receipt.msgs.ReceiptVersion;

public class IncomingProcessedMessageListener implements MessageListener {

	static Logger logger = LoggerFactory.getLogger(IncomingProcessedMessageListener.class);

	static XMPPServer xmppServer = XMPPServer.getInstance();

	
	

	@Override
	public void process(Message msg, Session session, boolean incoming, boolean processed) {
		if (incoming && processed) {
			if (!(session instanceof ClientSession)) {
				return;
			}

			if (!ReceiptMsgUtils.isNeedReceipt(msg) || ReceiptMsgUtils.isOfflineMsg(msg)
					|| StringUtils.isNotBlank(ReceiptMsgUtils.getMsgIdFromClientReceiptMsg(msg))) {
				return;
			}

			//服务端回执处理
			if (xmppServer.isLocal(msg.getFrom()) && versionMatch(msg.getFrom())) {
				sendSrvReceipt(msg, session);
			}

			String resource = null;
			if (Message.Type.chat == msg.getType()) {
				resource = msg.getFrom().getResource();
			} else if (Message.Type.groupchat == msg.getType()) {
				Element ele = msg.getElement().element(XConstants.RESOURCE);
				if (ele != null) {
					resource = ele.getText();
				}
				msg.getElement().remove(ele);
			}

			//兼容多终端
			Collection<ClientSession> toSessions = SessionManager.getInstance().getSessions(msg.getTo().getNode());
			if (CollectionUtils.isEmpty(toSessions)) {
				return;
			}

			for (ClientSession clntSess : toSessions) {
				JID toJID = clntSess.getAddress();
				try {
					toJID.toFullJID();
				} catch (Throwable t) {
					logger.error("receipt enQueue error! clntSess:{}", clntSess, t);
					continue;
				}

				//入队列处理
				if (!ReceiptMsgUtils.isSendToGroup(msg) && xmppServer.isLocal(toJID) && versionMatch(toJID)) {
					ClientSession clientSession = (ClientSession) session;
					MessageQueueWaitingReceipt queue = MessageQueueMap.get(clientSession);
					queue.offer(msg);

					if (ReceiptsProps.msgEnqueueLogEnable) {
						logger.error("msg enqueue:id:{},msg:{}", msg.getID(), msg.toXML());
					}
					break;//只要有一个入队列就ok
				}
			}

		}
	}

	private boolean versionMatch(JID fullJID) {
		String resource = fullJID.getResource();
		if (StringUtils.isBlank(resource)) {
			return false;
		}

		if (StringUtils.equals(JidResourceEnum.PC.getValue(), resource)) {
			return Versions.currentVersionIsGreatThan(fullJID, ReceiptVersion.pcVersion);
		}

		return Versions.currentVersionIsGreatThan(fullJID, ReceiptVersion.appVersion);
	}

	private void sendSrvReceipt(Message msg, Session session) {
		Message receiptMessage = ReceiptMsgUtils.createMsgReceipt(msg);
		session.deliverRawText(receiptMessage.toXML());
	}

	

}
