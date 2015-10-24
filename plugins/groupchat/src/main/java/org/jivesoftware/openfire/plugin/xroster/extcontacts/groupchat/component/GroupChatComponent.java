package org.jivesoftware.openfire.plugin.xroster.extcontacts.groupchat.component;

import java.util.concurrent.locks.Lock;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.openfire.RoutingTable;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.groupchat.enums.ProtocolEnum;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.groupchat.processor.MessageProcessor;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.CacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;

import org.jivesoftware.openfire.plugin.xroster.extcontacts.groupchat.helper.GroupChatCrossDomainHelper;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.groupchat.processor.IQProcessor;

public class GroupChatComponent implements Component {

	public static final String NAME = "groupchat";
	private final Logger LOG = LoggerFactory.getLogger(GroupChatComponent.class);
	private IQProcessor iqProcessor = IQProcessor.getInstance();
	private RoutingTable routingTable = XMPPServer.getInstance().getRoutingTable();

	private Cache<String, String> groupIdsCache;

	@Override
	public String getDescription() {
		return "groupchat component";
	}

	@Override
	public String getName() {
		return NAME;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(JID arg0, ComponentManager arg1) throws ComponentException {
		groupIdsCache = CacheFactory.createCache("groupIdsCache");
	}

	@Override
	public void processPacket(Packet fromPackage) {

		Packet packetCopy = fromPackage.createCopy();

		if (fromPackage instanceof Message) {
			Message fromMessage = (Message) fromPackage;
			processMessage(fromMessage);
		} else if (fromPackage instanceof IQ) {
			IQ fromIQ = (IQ) fromPackage;
			if (IQ.Type.error == fromIQ.getType() || IQ.Type.result == fromIQ.getType()) {
				return;
			}
			processIQ(fromIQ);
		}

		Packet crossDomainPacket = GroupChatCrossDomainHelper.duplicateForGroupChat(packetCopy);
		if (crossDomainPacket != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("duplicate groupChat Packet:{}", crossDomainPacket.toXML());
			}
			routingTable.routePacket(crossDomainPacket.getTo(), crossDomainPacket, true);
		}
	}

	private void processIQ(IQ request) {
		long start = System.currentTimeMillis();
		IQ reply = null;
		try {

			if (LOG.isInfoEnabled()) {
				LOG.info("[groupChat request,Thread:{},请求协议:],reqId:{},response:{}", Thread.currentThread().getId(),
						request.getID(), request.toXML());
			}

			Element childElement = request.getChildElement();
			String namespaceURI = childElement.getNamespaceURI();

			// 构造返回dom
			reply = IQ.createResultIQ(request);
			Element childEleCopy = childElement.createCopy();
			reply.setChildElement(childEleCopy);
			JID toJID = request.getTo();
			String toNode = null;
			if (toJID != null) {
				toNode = toJID.getNode();
			}

			// process different IQ by IQ protocal namespace URI
			if (ProtocolEnum.IQ_DISCO_INFO.getUrl().equals(namespaceURI)) {
				iqProcessor.discoInfo(request, reply);
				if (LOG.isInfoEnabled()) {
					LOG.info("[spend time:{}ms,Thread:{}],reqId:{},GroupChatComponent->discoInfo 服务发现response:{}",
							System.currentTimeMillis() - start, Thread.currentThread().getId(), request.getID(),
							reply.toXML());
				}
			} else if (ProtocolEnum.IQ_CREATE_GROUP.getUrl().equals(namespaceURI)) {
				Lock lock = CacheFactory.getLock(toNode.intern(), groupIdsCache);
				try {
					lock.lock();
					iqProcessor.createChatGroupIfNotExists(request, reply);
				} finally {
					lock.unlock();
				}

				if (LOG.isInfoEnabled()) {
					LOG.info(
							"[spend time:{}ms,Thread:{}],reqId:{},GroupChatComponent->createChatGroupIfNotExists() 创建组,response:{}",
							System.currentTimeMillis() - start, Thread.currentThread().getId(), request.getID(),
							reply.toXML());
				}

			} else if (ProtocolEnum.IQ_GET_CHAT_GROUPS.getUrl().equals(namespaceURI)) {
				iqProcessor.geSomeoneAllGroups(request, reply);

				if (LOG.isInfoEnabled()) {
					LOG.info(
							"[spend time:{}ms,Thread:{}],reqId:{},GroupChatComponent->geSomeoneAllGroups() 获取组列表,response:{}",
							System.currentTimeMillis() - start, Thread.currentThread().getId(), request.getID(),
							reply.toXML());
				}
			} else if (ProtocolEnum.IQ_GET_GROUP_MEMBERS.getUrl().equals(namespaceURI)) {
				iqProcessor.getSpecifiedGroupMembers(request, reply);

				if (LOG.isInfoEnabled()) {
					LOG.info(
							"[spend time:{}ms,Thread:{}],reqId:{},GroupChatComponent->getSpecifiedGroupMembers() 获取组成员列表,response:{}",
							System.currentTimeMillis() - start, Thread.currentThread().getId(), request.getID(),
							reply.toXML());
				}
			} else if (ProtocolEnum.IQ_GET_GROUPS_EXISTS.getUrl().equals(namespaceURI)) {
				iqProcessor.groupExists(request, reply);

				if (LOG.isInfoEnabled()) {
					LOG.info(
							"[spend time:{}ms,Thread:{}],reqId:{},GroupChatComponent->groupExists() 判断组是否出现,response:{}",
							System.currentTimeMillis() - start, Thread.currentThread().getId(), request.getID(),
							reply.toXML());
				}
			} else if (ProtocolEnum.IQ_UPDATE_GROUP_INFO.getUrl().equals(namespaceURI)) {
				Lock lock = CacheFactory.getLock(toNode.intern(), groupIdsCache);
				try {
					lock.lock();
					iqProcessor.updateGroupInfo(request, reply);
				} finally {
					lock.unlock();
				}

				if (LOG.isInfoEnabled()) {
					LOG.info("[spend time:{}ms,Thread:{}],reqId:{},GroupChatComponent->update() 更新组信息,response:{}",
							System.currentTimeMillis() - start, Thread.currentThread().getId(), request.getID(),
							reply.toXML());
				}
			} else if (ProtocolEnum.IQ_INVITE_MEMBERS.getUrl().equals(namespaceURI)) {
				Lock lock = CacheFactory.getLock(toNode.intern(), groupIdsCache);
				try {
					lock.lock();
					iqProcessor.inviteMembers(request, reply);
				} finally {
					lock.unlock();
				}

				if (LOG.isInfoEnabled()) {
					LOG.info(
							"[spend time:{}ms,Thread:{}],reqId:{},GroupChatComponent->inviteMembers() 邀请组成员,response:{}",
							System.currentTimeMillis() - start, Thread.currentThread().getId(), request.getID(),
							reply.toXML());
				}
			} else if (ProtocolEnum.CANCEL_MEMBER.getUrl().equals(namespaceURI)) {
				Lock lock = CacheFactory.getLock(toNode.intern(), groupIdsCache);
				try {
					lock.lock();
					iqProcessor.cancelMember(request, reply);
				} finally {
					lock.unlock();
				}

				if (LOG.isInfoEnabled()) {
					LOG.info("[spend time:{}ms,Thread:{}],reqId:{},GroupChatComponent->cancelMember() 退出组,response:{}",
							System.currentTimeMillis() - start, request.getID(), reply.toXML());
				}
			} else {
				reply.setError(PacketError.Condition.unexpected_request);
				if (LOG.isInfoEnabled()) {
					LOG.info("[spend time:{}ms],reqId:{},GroupChatComponent 非法请求 request: {},response:{}",
							System.currentTimeMillis() - start, request.getID(), request.toXML(), reply.toXML());
				}
			}

			ComponentManagerFactory.getComponentManager().sendPacket(this, reply);

		} catch (Exception e) {
			LOG.error("[spend time:{}ms],reqId:{},GroupChatComponent 请求处理异常,response:{}", System.currentTimeMillis()
					- start, request.toXML(), reply.toXML(), e);
		} finally {
			//
		}

	}

	/**
	 * process Presence package sended to externalcontact.servyou
	 *
	 * @param presence
	 */
	private void processPresence(Presence presence) {

	}

	/**
	 * process Message package sended to groupchat.servyou
	 *
	 * @param message
	 */
	private void processMessage(final Message message) {
		long start = System.currentTimeMillis();
		try {
			if (message.getType().toString().equals(Message.Type.groupchat.toString())) {
				//先存历史消息
				String msgId = StringUtils.isNotBlank(message.getID()) ? message.getID() : String.valueOf("genBySrv:"
						+ System.currentTimeMillis());
				if (message.getElement().attribute(XConstants.ORIGINAL_ID) == null) {
					message.getElement().addAttribute(XConstants.ORIGINAL_ID, msgId);
				}
				Message msgCopy = message.createCopy();
				msgCopy.getElement().addElement(XConstants.TO_GROUP);//声明下该消息是用户发往组中的,注意用户发往组中的消息是先入库再转发
				String msg = msgCopy.toXML();
                //TODO 历史消息处理
//				if (message.getTo().getDomain().endsWith(message.getFrom().getDomain())) {
//					ChatMsg chatMsg = new ChatMsg(msgId, message.getFrom().getNode(), message.getTo().getNode(),
//							msg.getBytes().length, msg, Long.valueOf(message.getElement()
//									.element(ServyouConstants.TIME_STAMP).getText()));
//					OnlineMsgService.getInstance().sendGroupChatMessage(JsonUtils.fromObject(chatMsg));//将组聊消息转发到redis队列
//				}
				
				//转发组消息，从组中出去的消息不需要存离线消息
				MessageProcessor.getInstance().broadcastMsgToAllGroupMember(message);

				if (LOG.isInfoEnabled()) {
					LOG.info("[spend time:{}ms],GroupChatComponent succeed processMessage,message:{}",
							System.currentTimeMillis() - start, message.toXML());
				}

			}
		} catch (Exception e) {
			LOG.error("[spend time:{}ms],GroupChatComponent->processMessage() error! message_XML={}",
					System.currentTimeMillis() - start, message.toXML(), e);
		}

	}

	@Override
	public void shutdown() {

	}

	@Override
	public void start() {

	}

}
