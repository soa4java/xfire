package org.jivesoftware.openfire.plugin.xroster.groupchat.helper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.yanrc.app.common.util.UUIDGenerator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.of.common.domain.UserTicket;
import org.jivesoftware.of.common.message.PacketQueue;
import org.jivesoftware.of.common.node.ImNodes;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.plugin.xroster.groupchat.GroupChatRemoteApis;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

/**
 * Created by yanrc on 2015/5/22.
 */
public class GroupMessages {

	static Logger LOG = LoggerFactory.getLogger(GroupMessages.class);
	static XMPPServer server = XMPPServer.getInstance();

	public static void broadcastMsgToMembers(final Set<String> memberPids, final Message msg, final String mySelfPid,
			boolean onlyOnline) {
		if (CollectionUtils.isNotEmpty(memberPids)) {

			Map<String, List<UserTicket>> userTicketmap = null;

			if (onlyOnline) {
				userTicketmap = GroupChatRemoteApis.getOnlineUserTicketMap(memberPids);
			} else {
				userTicketmap = GroupChatRemoteApis.getUserTicketMap(memberPids);
			}

			if (MapUtils.isEmpty(userTicketmap)) {
				return;
			}

			for (String pid : memberPids) {
				List<UserTicket> userTickets = userTicketmap.get(pid);

				if (CollectionUtils.isEmpty(userTickets)) {
					continue;
				}

				if (pid.equals(mySelfPid)) {
					continue;
				}

				for (UserTicket userTicket : userTickets) {
					Message msgCopy = msg.createCopy();
					msgCopy.setTo(new JID(userTicket.getPid(), userTicket.getDm(), userTicket.getRs(), false));
					if (!onlyOnline) {
						msgCopy.getElement().addElement(XConstants.RESOURCE).setText(msgCopy.getFrom().getResource());
						msgCopy.setID(generateMsgId());
					}
					
					if (StringUtils.equalsIgnoreCase(userTicket.getNodeName(), ImNodes.nodeName)) {
						server.getPacketRouter().route(msgCopy);
					} else {
						PacketQueue.getInstance().add(userTicket.getNodeName(), msgCopy);
					}

				}
			}
		}
	}

	public static String generateMsgId() {
		return UUIDGenerator.getUuid();
	}

	//TODO 组成员发生变化发送消息通知
	//    public Set<Object> getRevsubs(String groupId) {
	//        return redisTemplate.boundSetOps(String.format(RedisConstants.SET_PRE_TEMP_REVSUB_GRP, groupId)).members();
	//    }
	//
	//    public void convertAndSend(String channel, Object message) {
	//        redisTemplate.convertAndSend(channel, message);
	//    }
	//
	//    public Set<Object> getGroupTempRevSubs(String groupId){
	//        return redisTemplate.boundSetOps(String.format(RedisConstants.SET_PRE_TEMP_REVSUB_GRP, groupId)).members();
	//    }

}
