package org.jivesoftware.openfire.plugin.xroster.internal.listener;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.yanrc.app.common.util.JsonUtils;
import net.yanrc.web.xweb.presence.api.PresenceSubscriptionApi;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.jivesoftware.of.common.domain.UserTicket;
import org.jivesoftware.of.common.domain.utils.DomainNodeJidCacheUtils;
import org.jivesoftware.of.common.enums.PresenceStatus;
import org.jivesoftware.of.common.enums.Resource;
import org.jivesoftware.of.common.spring.SpringContextHolder;
import org.jivesoftware.of.common.thread.XExecutor;
import org.jivesoftware.of.common.utils.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PresenceBroadcasts {

	static PresenceSubscriptionApi presenceSubscriptionApi;

	static {
		presenceSubscriptionApi = SpringContextHolder.getBean(PresenceSubscriptionApi.class);
	}

	private static final Logger logger = LoggerFactory.getLogger(PresenceBroadcasts.class);

	public static final String FINAL = "final";

	//通知本域上的订阅者
	public static void broadCastPresence(final Presence original, final Presence presence) {
		if (PresenceBroadcastor.presenceBroadcastEnable) {
			XExecutor.globalExecutor.submit((new Runnable() {
				@Override
				public void run() {
					PresenceBroadcastor.broadcastPresence(original, presence.createCopy());
				}
			}));
		}
	}

	/**
	 * 根据之前的状态与当前presence消息比较，确定是否广播
	 */
	public static boolean needBroadcast(Presence presence) {

		if (presence.getExtension("x", "vcard-temp:x:update") != null) {//如果是个人信息修改发出的presence，需广播
			return true;
		}

		if (isFinal(presence)) {//是直接出席信息
			return true;
		}

		JID from = presence.getFrom();
		String personId = from.getNode();
		String tenantId = SessionUtils.getTopGroupId(from);

		Map<String, String> UserTicketStrMap = presenceSubscriptionApi.getUserTicket(tenantId, personId).getModel();

		if (MapUtils.isEmpty(UserTicketStrMap)) {
			addFinal(presence);
			return true;
		}

		Map<Resource, UserTicket> UserTicketMap = Maps.newHashMap();
		for (Map.Entry<String, String> entry : UserTicketStrMap.entrySet()) {
			UserTicketMap.put(Resource.fromCode(entry.getKey()), JsonUtils.toBean(entry.getValue(), UserTicket.class));
		}

		List<UserTicket> domainValues = Lists.newArrayList(UserTicketMap.values());
		Collections.sort(domainValues);
		UserTicket tailOld = domainValues.get(domainValues.size() - 1);

		//删除同类型的状态
		UserTicket userTicket = DomainNodeJidCacheUtils.generateUserTicket(presence);

		String resource = userTicket.getRs();
		for (Iterator<UserTicket> it = domainValues.iterator(); it.hasNext();) {
			UserTicket c = it.next();
			if (StringUtils.equalsIgnoreCase(c.getRs(), resource)) {
				it.remove();
			}
		}

		domainValues.add(userTicket);
		Collections.sort(domainValues);
		UserTicket tailNew = domainValues.get(domainValues.size() - 1);

		//不相同即需要广播
		boolean broadcast = tailOld != tailNew;

		if (broadcast) {
			addFinal(presence);
			//需要修改presence消息
			if (Resource.fromCode(tailNew.getRs()) != Resource.fromCode(presence.getFrom().getResource())) {
				JID jid = presence.getFrom();
				presence.setFrom(new JID(jid.getNode(), jid.getDomain(), tailNew.getRs()));
				presence.setNetstatus(tailNew.getNs());
				presence.setStatus(tailNew.getSs());

				if (PresenceStatus.fromCode(tailNew.getSs()) == PresenceStatus.INVAL) {
					presence.setType(Presence.Type.unavailable);
				} else {
					presence.setType(null);

				}

				//只有type为空，show才可以不为空
				if (Resource.fromCode(tailNew.getRs()) == Resource.PC) {
					try {
						presence.setShow(Enum.valueOf(Presence.Show.class, tailNew.getSo()));
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						presence.setShow(null);
					}
				} else {
					presence.setShow(null);
				}
			}
		}

		return broadcast;
	}

	/**
	 * 如不存在final属性，则增加该属性，值为true，标志该presence消息跨域时不检查redis中状态信息，直接广播
	 */
	public static void addFinal(Presence presence) {
		if (presence.getElement().attributeValue(FINAL) == null) {
			presence.getElement().addAttribute(FINAL, "true");
		}
	}

	public static boolean isFinal(Presence presence) {
		return "true".equals(presence.getElement().attributeValue(FINAL));
	}
}
