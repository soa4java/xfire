package org.jivesoftware.of.common.domain.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.jivesoftware.of.common.domain.DomainNodeJidCache;
import org.jivesoftware.of.common.domain.UserTicket;
import org.jivesoftware.of.common.enums.Resource;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 本地跨域信息缓存，这些数据是从redis同步过来的
 *
  */
public class LocalDomainNodeJidCache extends AbstractDomainNodeJidCache implements DomainNodeJidCache {
	private static final Logger LOGGER = getLogger(LocalDomainNodeJidCache.class);
	/**
	 * Map<personId, Map<resource, crossDomainInfo>>
	 */
	public static ConcurrentMap<String, Map<Resource, UserTicket>> localCrossDomainInfoMap = Maps.newConcurrentMap();
	private volatile static LocalDomainNodeJidCache INSTANCE;

	private LocalDomainNodeJidCache() {
		invalAll();
	}

	public static LocalDomainNodeJidCache getInstance() {
		if (INSTANCE == null) {
			synchronized (LocalDomainNodeJidCache.class) {
				if (INSTANCE == null) {
					INSTANCE = new LocalDomainNodeJidCache();
				}
			}
		}
		return INSTANCE;
	}

	@Override
	public void putDomain(UserTicket crossDomainInfo) {
		String personId = crossDomainInfo.getPid();
		if (StringUtils.isNotBlank(personId)) {
			Map<Resource, UserTicket> crossDomainInfoMap = localCrossDomainInfoMap.get(personId);
			if (crossDomainInfoMap == null) {
				crossDomainInfoMap = Maps.newConcurrentMap(); //同步map
				Map<Resource, UserTicket> old = localCrossDomainInfoMap.putIfAbsent(personId, crossDomainInfoMap);
				if (old != null) {
					//如已存在，使用老的
					crossDomainInfoMap = old;
				}
			}
			String resource = crossDomainInfo.getRs();
			if (StringUtils.isNotBlank(resource)) {
				crossDomainInfoMap.put(Resource.fromCode(resource), crossDomainInfo);
			}
		}
	}

	@Override
	public Map<String, Map<Resource, UserTicket>> getAllDomain() {
		return localCrossDomainInfoMap;
	}

	@Override
	public void putAllDomain(List<UserTicket> crossDomainInfoList) {
		for (UserTicket crossDomainInfo : crossDomainInfoList) {
			putDomain(crossDomainInfo);
		}
	}

	@Override
	public Map<String, UserTicket> multiFetchDomain(Collection<Object> personIds) {
		Map<String, UserTicket> crossDomainInfoMap = Maps.newHashMap();
		for (Object object : personIds) {
			String personId = (String) object;
			if (StringUtils.isNotBlank(personId)) {
				Map<Resource, UserTicket> paramMap = localCrossDomainInfoMap.get(personId);
				if (MapUtils.isNotEmpty(paramMap)) {
					List<UserTicket> crossDomainMap = Lists.newArrayList();
					crossDomainMap.addAll(paramMap.values());
					Collections.sort(crossDomainMap);
					UserTicket crossDomainInfo = crossDomainMap.get(crossDomainMap.size() - 1);

					if (crossDomainInfo != null) {
						crossDomainInfoMap.put(personId, crossDomainInfo);
					}
				}
			}
		}
		return crossDomainInfoMap;
	}

	@Override
	public Map<String, Map<Resource, UserTicket>> multiGetDomain(Collection<Object> personIds) {
		Map<String, Map<Resource, UserTicket>> crossDomainInfoMapMap = Maps.newHashMap();
		for (Object object : personIds) {
			String personId = (String) object;
			if (StringUtils.isNotBlank(personId)) {
				Map<Resource, UserTicket> crossDomainInfoMap = localCrossDomainInfoMap.get(personId);
				if (crossDomainInfoMap != null) {
					crossDomainInfoMapMap.put(personId, crossDomainInfoMap);
				}
			}
		}
		return crossDomainInfoMapMap;
	}

	@Override
	public Map<Resource, UserTicket> getDomain(String personId) {
		Map<Resource, UserTicket> crossDomainInfoMap = localCrossDomainInfoMap.get(personId);
		if (crossDomainInfoMap == null) {
			return Maps.newHashMap();
		}
		return crossDomainInfoMap;
	}
}
