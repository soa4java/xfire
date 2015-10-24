package org.jivesoftware.of.common.domain.utils;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.of.common.domain.DomainNodeJidCache;
import org.jivesoftware.of.common.domain.UserTicket;
import org.jivesoftware.of.common.domain.impl.LocalDomainNodeJidCache;
import org.jivesoftware.of.common.domain.impl.RedisDomainNodeJidCacheImpl;
import org.jivesoftware.of.common.enums.DomainEnum;
import org.jivesoftware.of.common.enums.NetStatusEnum;
import org.jivesoftware.of.common.enums.PresenceStatus;
import org.jivesoftware.of.common.enums.Resource;
import org.jivesoftware.of.common.enums.ShowStatusEnum;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 跨域信息工具方法
 *
 */
public class DomainNodeJidCacheUtils {
	private static final Logger LOGGER = getLogger(DomainNodeJidCacheUtils.class);

	private static DomainNodeJidCache redisCrossDomainInfoCache = RedisDomainNodeJidCacheImpl.getInstance();
	private static DomainNodeJidCache localCrossDomainInfoCache = LocalDomainNodeJidCache.getInstance();
	private static String xmppDomain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();

	/**
	 * 更新跨域信息，包括本地缓存和共享缓存(如果是被转发的服务器，则不必更新到共享缓存)
	 * @param presence
	 */
	public static void update(Presence presence) {
		UserTicket crossDomainInfo = generateUserTicket(presence);
		LocalDomainNodeJidCache.getInstance().putDomain(crossDomainInfo);

		// 如果是被转发的服务器，则不必更新到共享缓存
		if (StringUtils.equalsIgnoreCase(xmppDomain, crossDomainInfo.getDm())) {
			try {
				redisCrossDomainInfoCache.putDomain(crossDomainInfo);
			} catch (Throwable t) {
				LOGGER.error("updateCrossDomainInfo error!", t);
			}
		}
	}

	public static Map<String, UserTicket> multiFetch(Collection<Object> personIds) {
		Map<String, UserTicket> crossDomainInfoMap = localCrossDomainInfoCache.multiFetchDomain(personIds);
		if (MapUtils.isEmpty(crossDomainInfoMap)) {
			crossDomainInfoMap = redisCrossDomainInfoCache.multiFetchDomain(personIds);
		}
		// 有些用户的跨域信息可能拿不到，就生成默认的跨域信息
		for (Object object : personIds) {
			String personId = (String) object;
			if (StringUtils.isNotBlank(personId) && crossDomainInfoMap.get(personId) == null) {
				UserTicket crossDomainInfo = generateDefaultCrossDomainInfo(personId);
				crossDomainInfoMap.put(personId, crossDomainInfo);
			}
		}
		return crossDomainInfoMap;
	}

	private static UserTicket generateDefaultCrossDomainInfo(String personId) {
		return new UserTicket(personId, DomainEnum.DEFAULT.getValue(), Resource.PC.getCode(),
				PresenceStatus.INVAL.code(), ShowStatusEnum.EMPTY.getValue(), NetStatusEnum.PC.getValue(),
				JiveGlobals.getProperty(XConstants.HEART_BEAT_NODE_ID_KEY), System.currentTimeMillis(), "");
	}

	public static Map<String, Map<Resource, UserTicket>> multiGet(Collection<Object> personIds) {
		Map<String, Map<Resource, UserTicket>> crossDomainInfoMapMap = localCrossDomainInfoCache
				.multiGetDomain(personIds);
		if (MapUtils.isEmpty(crossDomainInfoMapMap)) {
			crossDomainInfoMapMap = redisCrossDomainInfoCache.multiGetDomain(personIds);
		}
		// 有些用户的跨域信息可能拿不到，就生成默认的跨域信息
		for (Object object : personIds) {
			String personId = (String) object;
			Map<Resource, UserTicket> crossDomainInfoMap = crossDomainInfoMapMap.get(personId);
			if (StringUtils.isNotBlank(personId) && MapUtils.isEmpty(crossDomainInfoMap)) {
				UserTicket crossDomainInfo = generateDefaultCrossDomainInfo(personId);
				if (crossDomainInfoMap == null) {
					crossDomainInfoMap = Maps.newHashMap();
				}
				crossDomainInfoMap.put(Resource.PC, crossDomainInfo);
				crossDomainInfoMapMap.put(personId, crossDomainInfoMap);
			}
		}
		return crossDomainInfoMapMap;
	}

	/**
	 * 获取域信息，优先获取当前资源的域; 其次获取pc端; 如果还为空，生成默认的
	 * @param personId
	 * @param jidResource
	 * @return
	 */
	public static UserTicket get(String personId, Resource jidResource) {
		Map<Resource, UserTicket> crossDomainInfoMap = localCrossDomainInfoCache.getDomain(personId);
		if (MapUtils.isEmpty(crossDomainInfoMap)) {
			crossDomainInfoMap = redisCrossDomainInfoCache.getDomain(personId);
		}
		// 有些用户的跨域信息可能拿不到，就生成默认的跨域信息
		if (MapUtils.isEmpty(crossDomainInfoMap)) {
			return generateDefaultCrossDomainInfo(personId);
		}
		if (jidResource == null) {
			jidResource = Resource.PC;
		}

		UserTicket crossDomainInfo = crossDomainInfoMap.get(jidResource);
		if (crossDomainInfo == null) {//如果不是pc就取第一个
			crossDomainInfo = crossDomainInfoMap.values().iterator().next();
		}
		return crossDomainInfo;
	}

	/**
	 *获取优先级最高的域信息
	 *
	 * @param personId
	 * @return
	 */
	public static UserTicket get(String personId) {
		Map<Resource, UserTicket> crossDomainInfoMap = localCrossDomainInfoCache.getDomain(personId);
		if (MapUtils.isEmpty(crossDomainInfoMap)) {
			crossDomainInfoMap = redisCrossDomainInfoCache.getDomain(personId);
		}
		// 有些用户的跨域信息可能拿不到，就生成默认的跨域信息
		if (MapUtils.isEmpty(crossDomainInfoMap)) {
			return generateDefaultCrossDomainInfo(personId);
		}

		List<UserTicket> crossDomainInfoList = Lists.newArrayList();
		crossDomainInfoList.addAll(crossDomainInfoMap.values());
		UserTicket crossDomainInfo;

		if (crossDomainInfoList.size() == 1) {
			crossDomainInfo = crossDomainInfoList.get(0);
		} else {
			Collections.sort(crossDomainInfoList);
			crossDomainInfo = crossDomainInfoList.get(crossDomainInfoList.size() - 1);
		}

		// crossDomainInfo还是有可能为null
		if (crossDomainInfo == null) {
			return generateDefaultCrossDomainInfo(personId);
		}

		return crossDomainInfo;
	}

	public static UserTicket generateUserTicket(Presence presence) {
		JID jid = presence.getFrom();
		String netstatus = null;
		Element netstatusEle = presence.getElement().element("netstatus");
		if (netstatusEle != null) {
			netstatus = netstatusEle.getText();
		} else {
			netstatus = "";
		}
		Presence.Show pshow = presence.getShow();
		String status = getStatus(presence);
		String show = (null != pshow ? pshow.name() : ShowStatusEnum.EMPTY.getValue());
		Resource resource = Resource.fromCode(jid.getResource());
		return new UserTicket(jid.getNode(), jid.getDomain(), resource == null ? Resource.PC.getCode()
				: resource.getCode(), status, show, netstatus,
				JiveGlobals.getProperty(XConstants.HEART_BEAT_NODE_ID_KEY), System.currentTimeMillis(), "");
	}

	/**
	 * 根据presence消息来判断status
	 */
	private static String getStatus(Presence presence) {
		String status = presence.getStatus();
		//status元素不为空，来自PC
		if (StringUtils.isNotBlank(status)) {
			return status;
		}
		//手机端只有在线、不在线，根据type属性判断
		if (presence.isAvailable()) {
			return PresenceStatus.AVAIL.code();
		}
		return PresenceStatus.INVAL.code();
	}
}
