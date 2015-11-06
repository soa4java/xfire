package org.jivesoftware.openfire.plugin.xroster.groupchat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.yanrc.web.xweb.presence.api.PresenceSubscriptionApi;

import org.jivesoftware.of.common.domain.UserTicket;
import org.jivesoftware.of.common.spring.SpringContextHolder;

public class GroupChatRemoteApis {
	
	private static PresenceSubscriptionApi presenceSubscriptionApi;

	static {
		presenceSubscriptionApi = SpringContextHolder.getBean(PresenceSubscriptionApi.class);
	}
	
	public static Map<String, List<UserTicket>> getOnlineUserTicketMap(Set<String> pids){
		return presenceSubscriptionApi.getOnlineUserTicketMap(pids).getModel();
	}
	
	public static Map<String, List<UserTicket>> getUserTicketMap(Set<String> pids){
		return presenceSubscriptionApi.getUserTicketMap(pids).getModel();
	}
	

}
