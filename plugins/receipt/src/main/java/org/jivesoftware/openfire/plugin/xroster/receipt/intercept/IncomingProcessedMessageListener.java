package org.jivesoftware.openfire.plugin.xroster.receipt.intercept;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.of.common.message.Messages;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.plugin.xroster.receipt.ReceiptsProps;
import org.jivesoftware.openfire.plugin.xroster.receipt.msgs.MessageQueueMap;
import org.jivesoftware.openfire.plugin.xroster.receipt.msgs.MessageQueueWaitingReceipt;
import org.jivesoftware.openfire.plugin.xroster.receipt.msgs.ReceiptMsgUtils;
import org.jivesoftware.openfire.plugin.xroster.receipt.msgs.ReceiptVersions;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class IncomingProcessedMessageListener implements PacketInterceptor {

	static Logger logger = LoggerFactory.getLogger(IncomingProcessedMessageListener.class);

	static XMPPServer xmppServer = XMPPServer.getInstance();

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {

		if (incoming && processed) {

			if (!(packet instanceof Message)) {
				return;
			}

			Message msg = (Message) packet;

			if (Message.Type.normal == msg.getType() || StringUtils.isBlank(msg.getType().toString())) {
				return;
			}

			if (!(session instanceof ClientSession)) {
				return;
			}

			if (!ReceiptMsgUtils.isNeedReceipt(msg) || ReceiptMsgUtils.isOfflineMsg(msg)
					|| StringUtils.isNotBlank(ReceiptMsgUtils.getMsgIdFromClientReceiptMsg(msg))) {
				return;
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

			for (ClientSession toClientSess : toSessions) {
				JID toJID = toClientSess.getAddress();
				try {
					toJID.toFullJID();
				} catch (Throwable t) {
					logger.error("receipt enQueue error! clntSess:{}", toClientSess, t);
					continue;
				}

				//根据接受方版本判断，是否需要将消息放入队列，如果接收方多个终端在线，只有一个终端回复了就ok
				if (!ReceiptMsgUtils.isSendToGroup(msg) && xmppServer.isLocal(toJID)
						&& ReceiptVersions.versionMatch(toJID) && !(Messages.isControlMessage(msg))) {

					if (toClientSess instanceof ClientSession) {
						ClientSession toClientSession = (ClientSession) toClientSess;
						MessageQueueWaitingReceipt queue = MessageQueueMap.get(toClientSession);
						queue.offer(msg);
						if (ReceiptsProps.msgEnqueueLogEnable) {
							logger.error("msg enqueue:id:{},toJID:{},msg:{}", msg.getID(), toJID, msg.toXML());
						}
					}
				}
			}

		}
	}

}
