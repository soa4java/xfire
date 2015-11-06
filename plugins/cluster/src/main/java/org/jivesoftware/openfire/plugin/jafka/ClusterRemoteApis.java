package org.jivesoftware.openfire.plugin.jafka;

import java.util.List;
import java.util.Map;

import net.yanrc.web.xweb.presence.api.PresenceSubscriptionApi;
import net.yanrc.web.xweb.presence.domain.SubRelationLifecycle;

import org.jivesoftware.of.common.domain.UserTicket;
import org.jivesoftware.of.common.spring.SpringContextHolder;

public class ClusterRemoteApis {
	
	private static PresenceSubscriptionApi presenceSubscriptionApi;

	static {
		presenceSubscriptionApi = SpringContextHolder.getBean(PresenceSubscriptionApi.class);
	}
	
	public static Map<String, UserTicket> getUserTicket(String tenantId, String personId){
		return presenceSubscriptionApi.getUserTicket(tenantId,personId).getModel();
	}
	

	public static List<SubRelationLifecycle> getSubscribersLifecycles(String personId,String resourceCode,String tenantId){
		return presenceSubscriptionApi.getSubscribersLifecycles(
				tenantId, personId, resourceCode).getModel();
	}

}
