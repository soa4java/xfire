package org.jivesoftware.openfire.plugin.xroster.groupchat.processor;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.yanrc.app.common.result.Result;
import net.yanrc.app.common.util.UUIDGenerator;
import net.yanrc.web.xweb.contacts.biz.service.RecentContactsApi;
import net.yanrc.web.xweb.groupchat.biz.api.GroupChatApi;
import net.yanrc.web.xweb.groupchat.domain.GroupInfo;
import net.yanrc.web.xweb.groupchat.query.MembersGetQuery;

import org.apache.commons.collections.CollectionUtils;
import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.of.common.domain.UserTicket;
import org.jivesoftware.of.common.enums.ImPotocal;
import org.jivesoftware.of.common.prop.Properties;
import org.jivesoftware.of.common.spring.SpringContextHolder;
import org.jivesoftware.of.common.thread.XExecutor;
import org.jivesoftware.of.common.utils.SessionUtils;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.plugin.xroster.groupchat.helper.GroupChatCrossDomainHelper;
import org.jivesoftware.openfire.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketExtension;

public class MessageProcessor extends AbstractProcessor {
	private static MessageProcessor instance = new MessageProcessor();
	private final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);
	private XMPPServer server = XMPPServer.getInstance();
	private String localDomain = server.getServerInfo().getXMPPDomain();
	private GroupChatApi groupChatApi;
	private RecentContactsApi recentContactsApiConsumer;

	private MessageProcessor() {
		groupChatApi = SpringContextHolder.getBean(GroupChatApi.class);
		recentContactsApiConsumer = SpringContextHolder.getBean(RecentContactsApi.class);
	}

	public static MessageProcessor getInstance() {
		if (instance == null) {
			synchronized (MessageProcessor.class) {
				if (instance == null) {
					instance = new MessageProcessor();
				}
			}
		}
		return instance;
	}

	public void broadcastMsgToAllGroupMember(final Message message) {

		if (message.getElement().attribute(XConstants.ORIGINAL_ID) == null) {
			message.getElement().addAttribute(XConstants.ORIGINAL_ID, message.getID());//保留原始id
		}

		JID fromJID = message.getFrom();
		JID groupJID = message.getTo().asBareJID();
		String groupId = groupJID.getNode();
		String tenantId = SessionUtils.getTopGroupId(fromJID);

		Result<GroupInfo> result = groupChatApi.queryMemberIds(new MembersGetQuery(groupId, tenantId));

		if (!result.isSuccess()) {
			LOG.error("group not exists when broadcastMsgToAllGroupMember:{}", message.toXML());
			return;
		}

		final Set<String> memberIds = result.getModel().getMemberIds();

		Map<String, UserTicket> pidMapInOtherDomain = GroupChatCrossDomainHelper.getPidAndDomainMapInOther(memberIds);
		String mySelfPid = fromJID.getNode();

		for (String pid : memberIds) {
			Message msgCopy = message.createCopy();
			msgCopy.getElement().addElement(XConstants.GRP_FROM_JID).setText(fromJID.toString());//给msg增加一个from节点，便于在广播给自己其他终端时，区分其他终端。

			if (pidMapInOtherDomain != null && null != pidMapInOtherDomain.get(pid)) {
				continue;//在其他域中的用户不处理
			}

			if (mySelfPid.equals(pid)) {
				//广播给自己其他终端
				broadcastToSelfOtherTerminals(msgCopy);
				continue;
			}

			//广播给组中在本域中的其他用户
			msgCopy.setFrom(new JID(groupJID.toBareJID() + "/" + mySelfPid));
			JID toBareJid = new JID(pid, localDomain, null, false);
			msgCopy.setTo(toBareJid.toBareJID());//to为bared才能多终端转发
			msgCopy.getElement().addElement(XConstants.RESOURCE).setText(message.getFrom().getResource());
			msgCopy.setID(generateMsgId());
			server.getPacketRouter().route(msgCopy);
		}

		if (Properties.recentContactEnable) {
			XExecutor.groupChatExecutor.submit(new Runnable() {
				@Override
				public void run() {
					saveContactsAndMesageDigest(message, memberIds);
				}
			});
		}

	}

	private void saveContactsAndMesageDigest(final Message message, final Set<String> memberIds) {
		try {
			String groupId = message.getTo().getNode();

			if (CollectionUtils.isEmpty(memberIds)) {
				return;
			}

			//所有组成员的最近联系人都有这个组
			recentContactsApiConsumer.saveContactsAndMesageDigest(true, groupId, memberIds, message.toXML());

		} catch (Throwable t) {
			LOG.error("cacheRecentContacts error! msg:{}", message.toXML(), t);
		}

	}

	public void broadcastToSelfOtherTerminals(Message msgCopy) {
		JID from = msgCopy.getFrom();
		String pid = msgCopy.getFrom().getNode();
		msgCopy.setID(generateMsgId());
		msgCopy.addExtension(new PacketExtension(ImPotocal.SynToSelf.extName(), ImPotocal.SynToSelf.extNamspace()));
		JID groupJID = msgCopy.getTo().asBareJID();
		msgCopy.setFrom(new JID(groupJID.toBareJID() + "/" + pid));
		msgCopy.setTo(from.toBareJID());//to为bared才能多终端转发
		//		server.getPacketRouter().route(msgCopy);

		String resouce = from.getResource();
		Collection<ClientSession> colls = SessionManager.getInstance().getSessions(pid);

		if (null == colls || colls.isEmpty()) {
			return;
		}

		for (ClientSession session : colls) {//转发给自己的时候需要过滤掉自己的当前终端资源
			if (resouce.equals(session.getAddress().getResource())) {
				continue;
			}
			msgCopy.setTo(session.getAddress());
			session.deliverRawText(msgCopy.toXML());
		}
	}

	private String generateMsgId() {
		return UUIDGenerator.getUuid();
	}
}
