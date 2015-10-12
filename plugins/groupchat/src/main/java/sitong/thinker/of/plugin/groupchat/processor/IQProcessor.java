package sitong.thinker.of.plugin.groupchat.processor;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.yanrc.app.common.result.Result;
import net.yanrc.web.xweb.groupchat.biz.service.GroupApi;
import net.yanrc.web.xweb.groupchat.biz.service.MemberApi;
import net.yanrc.web.xweb.groupchat.domain.Group;
import net.yanrc.web.xweb.groupchat.domain.GroupKey;
import net.yanrc.web.xweb.groupchat.domain.Member;
import net.yanrc.web.xweb.groupchat.dto.GroupDTO;
import net.yanrc.web.xweb.groupchat.query.GroupsGetQuery;
import net.yanrc.web.xweb.groupchat.query.MembersGetQuery;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.of.common.domain.DomainNodeJid;
import org.jivesoftware.of.common.error.ErrorCodeEnumOfGrobal;
import org.jivesoftware.of.common.error.XmppErrorMessageUtils;
import org.jivesoftware.of.common.spring.SpringContextHolder;
import org.jivesoftware.of.common.thread.XExecutor;
import org.jivesoftware.of.common.utils.JidUtil;
import org.jivesoftware.of.common.utils.SessionUtils;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketError;

import sitong.thinker.of.plugin.groupchat.component.GroupChatComponent;
import sitong.thinker.of.plugin.groupchat.enums.GroupCatEnum;
import sitong.thinker.of.plugin.groupchat.enums.GroupTypeEnum;
import sitong.thinker.of.plugin.groupchat.enums.MemberAffiliationEnum;
import sitong.thinker.of.plugin.groupchat.enums.MemberRoleEnum;
import sitong.thinker.of.plugin.groupchat.enums.ProtocolEnum;
import sitong.thinker.of.plugin.groupchat.enums.UserAttrEnum;
import sitong.thinker.of.plugin.groupchat.helper.GroupChatCrossDomainHelper;

public class IQProcessor extends AbstractProcessor {

	private static XMPPServer server = XMPPServer.getInstance();
	private static IQProcessor instance = new IQProcessor();
	private final Logger LOG = LoggerFactory.getLogger(XConstants.LOG_GROUPCHAT);

	private MemberApi memberApiConsumer;
	private GroupApi groupApiConsumer;

	private IQProcessor() {
		memberApiConsumer = SpringContextHolder.getBean("memberApi", MemberApi.class);
		groupApiConsumer = SpringContextHolder.getBean("groupApi", GroupApi.class);
	}

	public static IQProcessor getInstance() {
		if (instance == null) {
			synchronized (IQProcessor.class) {
				if (instance == null) {
					instance = new IQProcessor();
				}
			}
		}
		return instance;
	}

	/**
	 * 服务发现
	 *
	 * @param req
	 * @param res
	 */
	public void discoInfo(IQ req, IQ res) {

		Element xChildElement = res.getChildElement();

		if (req.getType() != IQ.Type.get) {
			res.setError(PacketError.Condition.service_unavailable);
			return;
		}

		xChildElement.addElement("feature").addAttribute("var", ProtocolEnum.IQ_GROUP_CHAT.getUrl());
		Element ele = xChildElement.addElement("x", "jabber:x:data");
		ele.addAttribute("type", "result");
	}

	/**
	 * 如果组没有出现就创建组
	 *
	 * @param req
	 * @param res
	 */
	public void createChatGroupIfNotExists(IQ req, IQ res) {
		if (IQ.Type.set != req.getType() || null == req.getFrom() || null == req.getTo()) {
			res.setError(PacketError.Condition.unexpected_request);
			return;
		}

		JID fromJID = req.getFrom();
		JID groupBareJID = req.getTo().asBareJID();
		Element replyChildEle = res.getChildElement();

		String tenantId = SessionUtils.getTopGroupId(fromJID);
		String groupId = groupBareJID.getNode();
		//已经存在该房间，不需要新建
		if (groupApiConsumer.getByPrimaryKey(new GroupKey(groupId, tenantId)) != null) {
			return;
		}

		String groupName = getEletentText(replyChildEle.element("groupName"));
		String temporary = getEletentText(replyChildEle.element("temporary"));

		Date now = new Date();
		Group group = new Group(groupId, groupId, temporary, "1", now, now, 2000);

		String userJid = fromJID.getNode();
		Member owner = new Member(tenantId, userJid, userJid, groupId, groupName, groupName, "admin", now, now, "owner");

		if (groupApiConsumer.add(group, owner).isSuccess()) {
			appendItem(replyChildEle, fromJID.toFullJID(), owner.getAffiliationCode(), owner.getRoleCode());
		} else {
			String errorCode = ErrorCodeEnumOfGrobal.CREATE_GROUP_FAIL.code();
			String errorText = XmppErrorMessageUtils.getErrorText(errorCode);
			res.setChildElement(XmppErrorMessageUtils.appendErrorChildToIQ(req, res, errorCode, errorText));
		}
	}

	private String getEletentText(Element ele) {
		if (ele != null) {
			return ele.getText();
		} else {
			return "";
		}
	}

	/**
	 * 成员退出聊天组
	 *
	 * @param req
	 * @param res
	 */
	public void cancelMember(IQ req, IQ res) {
		if (req.getFrom() == null || IQ.Type.set != req.getType()) {
			res.setError(PacketError.Condition.unexpected_request);
			return;
		}

		Element revokeEle = req.getChildElement().element(UserAttrEnum.REVOKE.getValue());
		if (revokeEle == null) {
			return;
		}

		Element jidEle = revokeEle.element(UserAttrEnum.JID.getValue());
		if (jidEle == null) {
			return;
		}

		String userBareJid = new JID(new JID(jidEle.getText()).getNode(), localDomain, null).toBareJID();

		JID groupJID = req.getTo();
		String groupId = req.getTo().getNode();
		try {
			if (!JidUtil.canTransformToBareJid(userBareJid)) {
				res.setError(PacketError.Condition.bad_request);
				LOG.warn("reqId:{},cancel member jid invalidate(组成员jid非法),groupId:{},fromIQ:{}", req.getID(), req
						.getTo().getNode(), req.toXML());
				return;
			}
		} catch (Throwable t) {
			res.setError(PacketError.Condition.bad_request);
			LOG.error("reqId:{},cancel group member error,groupId:{},fromIQ:{}", req.getID(), req.getTo().getNode(),
					req.toXML());
			return;
		}

		String memberId = new JID(userBareJid).getNode();

		//退出成功，从该用户的最近联系组中清除该用户，删除该用户的在该组的离线消息。
		//TODO 清除最近联系人
		//        GroupMemberOffMsgCountClearer.getInstance().cleanGroupFromRecentContacts(req.getFrom().getNode(), groupId);
		//        GroupMemberOffMsgCountClearer.getInstance().removeOfflineMsgCount(req.getFrom().getNode(), groupId);

		String tenantId = SessionUtils.getTopGroupId(req.getFrom());
		Result<Set<String>> result = memberApiConsumer.remove(new GroupKey(groupId, tenantId), memberId);

		if (result != null && result.isSuccess()) {
			Set<String> members = result.getModel();
			JID localGroupJID = new JID(groupJID.getNode(), GroupChatComponent.NAME + "." + localDomain, null, false);
			Message cancelMsg = createCancelUserMsg(localGroupJID, userBareJid);

			if (CollectionUtils.isNotEmpty(members)) {
				broadcastMemberCancelMsg(req.getFrom(), groupId, members, cancelMsg);
			}
		}

	}

	/**
	 * 广播成员退出组
	 *
	 * @param mySelfJID
	 * @param groupId
	 * @param cancelMsg
	 */
	private void broadcastMemberCancelMsg(final JID mySelfJID, final String groupId, final Set<String> members,
			final Message cancelMsg) {
		final String mySelfPid = mySelfJID.getNode();
		final String resource = mySelfJID.getResource();
		XExecutor.globalExecutor.execute(new Runnable() {
			@Override
			public void run() {
				broadcastToMySelfOtherResources(mySelfPid, resource, cancelMsg);
				Map<String, DomainNodeJid> pidMapInOtherDomain = GroupChatCrossDomainHelper
						.getPidAndDomainMapInOther(members);
				for (String pid : members) {//给组中剩下的中除自己外的其他用户发送

					if (pidMapInOtherDomain != null && null != pidMapInOtherDomain.get(pid)) {
						continue;//在其他域中的用户不处理
					}

					if (CollectionUtils.isEmpty(SessionManager.getInstance().getSessions(pid))) {
						continue;
					}

					if (pid.equals(mySelfPid)) {
						continue;
					}

					Message msg = cancelMsg.createCopy();
					msg.setTo(new JID(pid, localDomain, null, false));
					server.getPacketRouter().route(msg);
				}
			}
		});
	}

	void broadcastToMySelfOtherResources(String myPid, String resource, Message msg) {
		Collection<ClientSession> colls = SessionManager.getInstance().getSessions(myPid);
		if (CollectionUtils.isNotEmpty(colls) && StringUtils.isNotBlank(resource)) {
			for (ClientSession session : colls) {//转发给自己的时候需要过滤掉自己的当前终端资源
				if (resource.equals(session.getAddress().getResource())) {
					continue;
				}
				Message msgCopy = msg.createCopy();
				msgCopy.setTo(session.getAddress());
				session.deliverRawText(msgCopy.toXML());
			}
		}
	}

	public Message createCancelUserMsg(JID groupJid, String userJid) {
		Message msg = new Message();
		msg.setFrom(groupJid);
		Element x = msg.addChildElement("x", ProtocolEnum.CANCEL_MEMBER.getUrl());
		msg.addChildElement("no-store", "urn:xmpp:hints");//该消息不存离线
		appendItem(x, userJid, null);
		return msg;
	}

	/**
	 * 邀请成员
	 *
	 * @param req
	 * @param res
	 */
	public void inviteMembers(IQ req, IQ res) {

		if (req.getFrom() == null || IQ.Type.set != req.getType()) {
			res.setError(PacketError.Condition.unexpected_request);
			return;
		}

		final Element inviterEle = req.getChildElement().element("inviter").createCopy();
		final Element partnerEle = req.getChildElement().element("partner");
		List jidsEles = req.getChildElement().element("invitee").elements("jid");
		String errorCode = null;

		// 被邀请人员列表为空
		if (jidsEles == null || jidsEles.isEmpty()) {
			errorCode = ErrorCodeEnumOfGrobal.INVITE_MEMEBERS_NOT_BLANK.code();
			res.setChildElement(XmppErrorMessageUtils.appendErrorChildToIQ(req, res, errorCode,
					XmppErrorMessageUtils.getErrorText(errorCode)));
			return;
		}

		final Set<String> toBeInvitedMemberJids = new HashSet<>(jidsEles.size());
		String myOwnBareJid = req.getFrom().toBareJID().toString();

		for (Object ele : jidsEles) {
			String jid = ((Element) ele).getText();
			//做一个数据格式验证，便于问题跟踪
			try {
				if (!JidUtil.canTransformToBareJid(jid)) {
					errorCode = ErrorCodeEnumOfGrobal.GROUP_MEMBER_JID_FORMATE_ERROR.code();
					res.setChildElement(XmppErrorMessageUtils.appendErrorChildToIQ(req, res, errorCode,
							XmppErrorMessageUtils.getErrorText(errorCode)));
					return;
				}
			} catch (Throwable t) {
				errorCode = ErrorCodeEnumOfGrobal.GROUP_MEMBER_JID_FORMATE_ERROR.code();
				res.setChildElement(XmppErrorMessageUtils.appendErrorChildToIQ(req, res, errorCode,
						XmppErrorMessageUtils.getErrorText(errorCode)));
				LOG.error("invite group member error,groupId:{},request:{}", req.getTo().getNode(), req.toXML());
				return;
			}

			String memberBareJid = new JID(new JID(jid).getNode(), localDomain, null, false).toBareJID();
			//不邀请自己
			if (StringUtils.isNotBlank(memberBareJid) && !memberBareJid.equals(myOwnBareJid)) {
				toBeInvitedMemberJids.add(memberBareJid);
			}
		}

		final String groupId = req.getTo().getNode();
		final String tenantId = SessionUtils.getTopGroupId(req.getFrom());

		Result<Group> result = groupApiConsumer.getByPrimaryKey(new GroupKey(groupId, tenantId));
		// 如果组已经删除
		if (result == null || result.getModel() == null) {
			errorCode = ErrorCodeEnumOfGrobal.GROUP_NOT_EXISTS.code();
			String errorText = XmppErrorMessageUtils.getErrorText(errorCode);
			res.setChildElement(XmppErrorMessageUtils.appendErrorChildToIQ(req, res, errorCode, errorText));
			return;
		}

		Group chatGroup = result.getModel();

		// 原始组成员
		Set<String> originalGroupMemberIds = memberApiConsumer.queryMemberIds(new MembersGetQuery(groupId, tenantId))
				.getModel();

		// 组中不存在的人员才会被添加。
		Set<Object> toBeInvitedMemberIds = getPidsByJids(toBeInvitedMemberJids);
		Set<String> allGroupMemberIds = memberApiConsumer.add(new GroupKey(groupId, tenantId), null,
				Set.class.cast(toBeInvitedMemberIds)).getModel();
		Set<String> allGroupMemberJids = getJidsByPids(allGroupMemberIds);
		Set<String> finalAdedMemberJids = toBeInvitedMemberJids;//默认认为被邀请的人都被加入了，暂时这样处理

		// 有成员被成功添加，才会广播新成员被加入。
		String groupName = chatGroup.getName();
		String groupCat = GroupTypeEnum.PERSIATENT.getValue().equals(chatGroup.getType()) ? GroupCatEnum.DISCUSS_GROUP
				.getValue() : GroupCatEnum.TEMP.getValue();

		//TODO 通知订阅服务有新成员加入组
		//		Set<Object> pidSet = getPidsByJids(finalAdedMemberJids);
		//		MemberChangedMsgSenderHelper.getInstance().convertAndSend(RedisConstants.TOPIC_MEB_CHANGE,
		//				JsonUtils.fromObject(new GroupMemberChangeMsg(groupId, pidSet.toArray(), null)));

		String localGroupJID = new JID(groupId, GroupChatComponent.NAME + "." + localDomain, null, false).toString();
		Message invitedMessage = createInvitedMsg(localGroupJID, groupName, groupCat, inviterEle, partnerEle,
				allGroupMemberJids);
		Message newMemberJoinMessage = createNewMemberJoinMsg(localGroupJID, finalAdedMemberJids);

		broadcastNewMemberJoin(groupId, req.getFrom(), invitedMessage, newMemberJoinMessage, toBeInvitedMemberJids,
				originalGroupMemberIds);
	}

	/**
	 * 广播新成员加入
	 *
	 * @param groupId
	 * @param mySelfJID
	 * @param invitedMessage
	 * @param newMemberJoinMessage
	 * @param toBeInvitedMemberJids
	 */
	private void broadcastNewMemberJoin(final String groupId, final JID mySelfJID, final Message invitedMessage,
			final Message newMemberJoinMessage, final Set<String> toBeInvitedMemberJids,
			final Set<String> originalGroupMemberIds) {
		final String mySelfPid = mySelfJID.getNode();
		final String resource = mySelfJID.getResource();
		XExecutor.groupChatExecutor.submit(new Runnable() {
			private static final long serialVersionUID = 2369881290609253552L;

			@Override
			public void run() {
				//给关注这个组所有人推送新成员加入消息
				//                Set<Object> groupTempRevSubs = MemberChangedMsgSenderHelper.getInstance().getGroupTempRevSubs(groupId);
				//                Set<String> groupTempRevSubPids = GroupChatHelper.getPids(groupTempRevSubs);
				broadcastToMySelfOtherResources(mySelfPid, resource, newMemberJoinMessage);
				if (CollectionUtils.isNotEmpty(originalGroupMemberIds)) {
					Map<String, DomainNodeJid> pidMapInOtherDomain = GroupChatCrossDomainHelper
							.getPidAndDomainMapInOther(originalGroupMemberIds);
					for (String pid : originalGroupMemberIds) {
						if (pidMapInOtherDomain != null && null != pidMapInOtherDomain.get(pid)) {
							continue;//在其他域中的用户不处理
						}

						if (CollectionUtils.isEmpty(SessionManager.getInstance().getSessions(pid))) {
							continue;
						}

						if (pid.equals(mySelfPid)) {
							continue;
						}

						Message newMemberJoinMsg = newMemberJoinMessage.createCopy();
						newMemberJoinMsg.setTo(new JID(pid, localDomain, null, false));
						server.getPacketRouter().route(newMemberJoinMsg);

						if (LOG.isInfoEnabled()) {
							LOG.info("邀请组成员告知旧成员协议groupId:{},xml:{}", groupId, newMemberJoinMsg.toXML());
						}
					}
				}

				//给被邀请人推送邀请协议
				if (CollectionUtils.isNotEmpty(toBeInvitedMemberJids)) {
					for (String addedMemberJid : toBeInvitedMemberJids) {
						Message invitedMsg = invitedMessage.createCopy();
						invitedMsg.setTo(addedMemberJid);
						server.getPacketRouter().route(invitedMsg);

						if (LOG.isInfoEnabled()) {
							LOG.info("邀请组成员协议groupId:{},xml:{}", groupId, invitedMsg.toXML());
						}
					}
				}
			}
		});
	}

	/**
	 * 创建新成员加入message
	 *
	 * @param groupJid  聊天组jid
	 * @param addedJids 被成功邀请的成员
	 * @return
	 */
	public Message createNewMemberJoinMsg(String groupJid, Set<String> addedJids) {
		Message msg = new Message();
		msg.setFrom(groupJid);
		Element x = msg.addChildElement("x", ProtocolEnum.MSG_NEW_MEMBERS.getUrl());
		msg.addChildElement("no-store", "urn:xmpp:hints");//该消息不存离线
		// 添加被邀请人到成员列表
		if (addedJids != null && !addedJids.isEmpty()) {
			for (String jidStr : addedJids) {
				JID jid = new JID(jidStr);
				Element itemEle = x.addElement("item");
				itemEle.addAttribute("jid", jid.toString());
				itemEle.addAttribute("nick", jid.getNode());
				itemEle.addAttribute("name", "");//目前不支持昵称，目前没有业务需求
				//                Presence presence = SessionUtils.getPresence(jid.getNode());
				itemEle.addAttribute("presence", ""/*presence != null ? presence.getStatus() : ServyouStateEnum.INVAL.code()*/);
			}
		}
		return msg;
	}

	/**
	 * 创建邀请消息
	 *
	 * @param groupJid     聊天组jid
	 * @param groupName    组名
	 * @param groupCat     组类型
	 * @param inviterEle
	 * @param groupMembers 组成员
	 * @return
	 */
	public Message createInvitedMsg(String groupJid, String groupName, String groupCat, Element inviterEle,
			Element partnerEle, Set<String> groupMembers) {
		Message msg = new Message();
		msg.setFrom(groupJid);
		Element x = msg.addChildElement("x", ProtocolEnum.MSG_IS_INVITED.getUrl());
		msg.addChildElement("no-store", "urn:xmpp:hints");//该消息不存离线
		/*	if(inviterEle!=null){
		        x.insert(inviterEle.createCopy());
			}*/
		if (partnerEle != null) {
			Element partner = x.addElement("partner");
			partner.addAttribute("jid", partnerEle.attributeValue("jid"));
		}

		Element inviter = x.addElement("inviter");
		inviter.addAttribute("jid", inviterEle.attributeValue("jid"));
		inviter.addAttribute("scene", inviterEle.attributeValue("scene"));

		x.addElement("groupJid").setText(groupJid);
		x.addElement("groupName").setText(groupName);
		x.addElement("groupCat").setText(groupCat);
		Element memberListEle = x.addElement("memberList");
		// 添加被邀请人到成员列表
		if (groupMembers != null && !groupMembers.isEmpty()) {
			for (String member : groupMembers) {
				memberListEle.addElement("jid").setText(member);
			}
		}
		return msg;
	}

	public void groupExists(IQ req, IQ res) {

		if (IQ.Type.get != req.getType() || req.getTo() == null) {
			res.setError(PacketError.Condition.unexpected_request);
			return;
		}

		String groupId = req.getTo().getNode();
		String tenantId = SessionUtils.getTopGroupId(req.getFrom());
		Result<Group> result = groupApiConsumer.getByPrimaryKey(new GroupKey(groupId, tenantId));
		Element statusEle = res.getChildElement().addElement("isExist");
		if (result != null && result.getModel() != null) {
			Group group = result.getModel();
			statusEle.setText("1");
			Element grpEle = res.getChildElement().addElement("group");
			grpEle.addAttribute("id", group.getId());
			grpEle.addAttribute("name", group.getName());
			grpEle.addAttribute("type", StringUtils.equals(group.getType(), GroupTypeEnum.TEMPORARY.getValue()) ? "1"
					: "0");
		} else {
			statusEle.setText("0");
		}
	}

	public void updateGroupInfo(IQ req, IQ res) {

		if (IQ.Type.set != req.getType() || req.getTo() == null) {
			res.setError(PacketError.Condition.unexpected_request);
			return;
		}

		String groupId = req.getTo().getNode();
		Element fromChildEle = req.getChildElement();
		String groupName = fromChildEle.element("groupName").getText();

		if (StringUtils.isBlank(groupName)) {
			groupName = groupId;
		}

		String groupOwners = fromChildEle.element("groupOwners").getText();
		String temporary = fromChildEle.element("temporary").getText();

		// 如果组已经删除
		String tenantId = SessionUtils.getTopGroupId(req.getFrom());
		Group group = groupApiConsumer.getByPrimaryKey(new GroupKey(groupId, tenantId)).getModel();
		if (null == group) {
			String errorCode = ErrorCodeEnumOfGrobal.GROUP_NOT_EXISTS.code();
			String errorText = XmppErrorMessageUtils.getErrorText(errorCode);
			res.setChildElement(XmppErrorMessageUtils.appendErrorChildToIQ(req, res, errorCode, errorText));
			return;
		} else {
			//禁止讨论组转临时组
			if (GroupTypeEnum.PERSIATENT.getValue().equals(group.getType())/*数据库中为持久组*/
					&& temporary.equals("1") /*请求中为要求变为临时组*/) {
				String errorCode = ErrorCodeEnumOfGrobal.PERSISTENCE_TO_TEMP.code();
				String errorText = XmppErrorMessageUtils.getErrorText(errorCode);
				XmppErrorMessageUtils.appendErrorChildToIQ(req, res, errorCode, errorText);
				LOG.warn("must not persistent group to temporary(持久组转临时组业务异常),groupId:{},fromIQ:{}", req.getTo()
						.getNode(), req.toXML());
				return;
			}
		}
		// 组信息设置失败
		GroupDTO groupDTO = new GroupDTO();
		groupDTO.setName(groupName);
		groupDTO.setType(temporary);

		Result<Integer> editCount = groupApiConsumer.editByPrimaryKeySelective(new GroupKey(groupId, tenantId),
				groupDTO);

		if (editCount == null || editCount.getModel() == null || editCount.getModel().intValue() == 0) {
			String errorCode = ErrorCodeEnumOfGrobal.GROUP_UPDATE_FAIL.code();
			String errorText = XmppErrorMessageUtils.getErrorText(errorCode);
			res.setChildElement(XmppErrorMessageUtils.appendErrorChildToIQ(req, res, errorCode, errorText));
			return;
		}
	}

	/**
	 * 获取用户所属聊天组列表
	 *
	 * @param req
	 * @param res
	 */
	public void getSpecifiedGroupMembers(IQ req, IQ res) {

		if (IQ.Type.get != req.getType() || req.getTo() == null) {
			res.setError(PacketError.Condition.unexpected_request);
			return;
		}

		String groupId = req.getTo().getNode();
		String tenantId = SessionUtils.getTopGroupId(req.getFrom());
		Element replyChildEle = res.getChildElement();

		if (groupApiConsumer.getByPrimaryKey(new GroupKey(groupId, tenantId)).getModel() == null) {
			String errorCode = ErrorCodeEnumOfGrobal.GROUP_NOT_EXISTS.code();
			String errorText = XmppErrorMessageUtils.getErrorText(errorCode);
			XmppErrorMessageUtils.appendErrorChildToIQ(req, res, errorCode, errorText);
			return;
		}

		Result<Set<String>> result = memberApiConsumer.queryMemberIds(new MembersGetQuery(groupId, tenantId));
		if (result != null && result.isSuccess()) {
			Set<String> groupMemberIds = result.getModel();
			if (CollectionUtils.isNotEmpty(groupMemberIds)) {
				for (String memberId : groupMemberIds) {
					try {
						Element itemEle = replyChildEle.addElement("item");
						itemEle.addAttribute("jid", memberId);
						itemEle.addAttribute("name", memberId);
						itemEle.addAttribute("affiliation", MemberAffiliationEnum.OUTCAST.getValue());
						itemEle.addAttribute("role", MemberRoleEnum.PARTICIPANT.getValue());
						itemEle.addAttribute("presence", "");
					} catch (Throwable t) {
						LOG.error("getSpecifiedGroupMembers() error!", t);
						continue;
					}
				}
			}
		}
	}

	private Set<Object> getPidsByJids(Set<String> groupMemberJidStrs) {
		Set<Object> set = new HashSet<>();
		for (String jidStr : groupMemberJidStrs) {
			set.add(new JID(jidStr).getNode());
		}
		return set;
	}

	Set<String> getJidsByPids(Set<String> groupMemberPids) {
		Set<String> set = new HashSet<>();
		for (String pid : groupMemberPids) {
			set.add(new JID(pid, localDomain, null).toString());
		}
		return set;
	}

	/**
	 * 获取用户所属聊天组列表
	 *
	 * @param req
	 * @param res
	 */
	public void geSomeoneAllGroups(IQ req, IQ res) {
		if (IQ.Type.get != req.getType()) {
			res.setError(PacketError.Condition.unexpected_request);
			return;
		}

		Element fromChildEle = req.getChildElement();
		String userJid = fromChildEle.element("member_jid").getText();
		String cat = fromChildEle.element("cat").getText();
		String groupType = GroupTypeEnum.getValueByCat(cat);

		Element replyChildEle = res.getChildElement();
		String tenantId = SessionUtils.getTopGroupId(req.getFrom());
		Result<List<Group>> reulst = groupApiConsumer.query(new GroupsGetQuery(tenantId, new JID(userJid).getNode(),
				groupType));
		List<Group> chatGroups = null;

		if (reulst == null || !reulst.isSuccess() || CollectionUtils.isEmpty(reulst.getModel())) {
			return;
		}

		chatGroups = reulst.getModel();

		for (Group group : chatGroups) {
			try {
				Element itemEle = replyChildEle.addElement("item");
				itemEle.addElement("is_temporary").setText(
						StringUtils.equals(group.getType(), GroupTypeEnum.TEMPORARY.getValue()) ? "1" : "0");
				itemEle.addElement("num_max_users").setText(String.valueOf(group.getMaxUser()));
				itemEle.addElement("cat").setText(cat);
				itemEle.addElement("is_password_protected").setText("0");
				itemEle.addElement("num_users").setText(String.valueOf(group.getMaxUser()));
				itemEle.addElement("subject").setText("");
				itemEle.addElement("name").setText(
						StringUtils.isNotBlank(group.getName()) ? group.getName() : "未命名讨论组.");
				itemEle.addElement("jid").setText(group.getId() + "@" + req.getTo());
				itemEle.addElement("is_member_only").setText("1");
			} catch (Throwable throwable) {
				LOG.error("IQProcessor.geSomeoneAllGroups() error!tentantId:{},groupType:{},userJid:{}", tenantId,
						groupType, userJid, throwable);
			}
		}
	}

}
