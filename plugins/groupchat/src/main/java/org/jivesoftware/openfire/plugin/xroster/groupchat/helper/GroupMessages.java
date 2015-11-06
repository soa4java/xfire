package org.jivesoftware.openfire.plugin.xroster.groupchat.helper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.of.common.domain.UserTicket;
import org.jivesoftware.of.common.message.PacketQueue;
import org.jivesoftware.openfire.plugin.xroster.groupchat.GroupChatRemoteApis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

/**
 * Created by yanrc on 2015/5/22.
 */
public class GroupMessages {

    static Logger LOG = LoggerFactory.getLogger(GroupMessages.class);
       
    public static  void broadcastMsgToOnlineMember(final Set<String> members, final Message msg, final String mySelfPid) {
		if (CollectionUtils.isNotEmpty(members)) {

			Map<String, List<UserTicket>> onlineGroupMemeberMap = GroupChatRemoteApis
					.getPidAndDomainMapForOnline(members);

			if (MapUtils.isEmpty(onlineGroupMemeberMap)) {
				return;
			}

			for (String pid : members) {
				List<UserTicket> userTickets = onlineGroupMemeberMap.get(pid);

				if (CollectionUtils.isEmpty(userTickets)) {
					continue;
				}

				if (pid.equals(mySelfPid)) {
					continue;
				}

				for (UserTicket userTicket : userTickets) {
					Message newMemberJoinMsg = msg.createCopy();
					newMemberJoinMsg.setTo(new JID(userTicket.getPid(), userTicket.getDm(), userTicket.getRs(), false));
					PacketQueue.getInstance().add(userTicket.getNodeName(), newMemberJoinMsg);
				}
			}
		}
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
