package org.jivesoftware.of.common.domain.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.yanrc.app.common.util.JsonUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.of.common.domain.DomainNodeJid;
import org.jivesoftware.of.common.domain.DomainNodeJidCache;
import org.jivesoftware.of.common.enums.JidResourceEnum;
import org.jivesoftware.of.common.spring.SpringContextHolder;
import org.jivesoftware.of.common.utils.ConfigUtils;
import org.jivesoftware.util.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RedisDomainNodeJidCacheImpl extends AbstractDomainNodeJidCache implements DomainNodeJidCache {

	private static final String KEY = "hash_user_domain";
	private static Logger LOG = LoggerFactory.getLogger(DomainNodeJidCache.class);
	private volatile static DomainNodeJidCache INSTANCE;
	private volatile RedisTemplate<String, Object> redisTemplate;

	@SuppressWarnings("unchecked")
	private RedisDomainNodeJidCacheImpl() {
		redisTemplate = SpringContextHolder.getBean(XConstants.REDIS_TEMPLATE, RedisTemplate.class);
		invalAll();
	}

	public static DomainNodeJidCache getInstance() {
		if (INSTANCE == null) {
			synchronized (DomainNodeJidCache.class) {
				if (INSTANCE == null) {
					INSTANCE = new RedisDomainNodeJidCacheImpl();
				}
			}
		}
		return INSTANCE;
	}

	Object multiGet(Collection<Object> personIds) {
		return redisTemplate.boundHashOps(KEY).multiGet(personIds);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, DomainNodeJid> multiFetchDomain(Collection<Object> personIds) {
		if (CollectionUtils.isEmpty(personIds)) {
			return Maps.newHashMap();
		}
		try {
			Object obj = multiGet(personIds);
			if (obj == null) {
				return Maps.newHashMap();
			}
			List<String> list = (List<String>) obj;

			Map<String, DomainNodeJid> crossDomainInfoMap = Maps.newHashMap();
			for (String string : list) {
				if (StringUtils.isBlank(string)) {
					continue;
				}
				Map<String, String> domainInfoMap = JsonUtils.toMap(string);
				//判断看有没有用pc登录过
				Map<JidResourceEnum, DomainNodeJid> paramMap = Maps.newHashMap();
				for (Entry<String, String> entry : domainInfoMap.entrySet()) {
					paramMap.put(JidResourceEnum.fromValue(entry.getKey()),
							JsonUtils.toBean(entry.getValue(), DomainNodeJid.class));
				}

				List<DomainNodeJid> crossDomainMap = Lists.newArrayList();
				crossDomainMap.addAll(paramMap.values());
				Collections.sort(crossDomainMap);
				DomainNodeJid crossDomainInfo = crossDomainMap.get(crossDomainMap.size() - 1);

				if (crossDomainInfo != null) {
					crossDomainInfoMap.put(crossDomainInfo.getPid(), crossDomainInfo);
				}
			}
			return crossDomainInfoMap;
		} catch (Throwable t) {
			LOG.error("CrossDomainInfoCache.multiFetch() error!, personIds:{} ", personIds, t);
			return Maps.newHashMap();
		}
	}

	@Override
	public Map<String, Map<JidResourceEnum, DomainNodeJid>> multiGetDomain(Collection<Object> personIds) {
		if (CollectionUtils.isEmpty(personIds)) {
			return Maps.newHashMap();
		}
		try {
			Object obj = multiGet(personIds);
			if (obj == null) {
				return Maps.newHashMap();
			}
			List<String> list = (List<String>) obj;
			Map<String, Map<JidResourceEnum, DomainNodeJid>> crossDomainInfoMapMap = Maps.newHashMap();
			for (String string : list) {
				if (StringUtils.isBlank(string)) {
					continue;
				}
				Map<JidResourceEnum, DomainNodeJid> crossDomainInfoMap = Maps.newHashMap();
				String personId = null;
				for (Map.Entry<String, String> entry : ((Map<String, String>) JsonUtils.toMap(string)).entrySet()) {
					if (StringUtils.isBlank(entry.getKey()) || StringUtils.isBlank(entry.getValue())) {
						continue;
					}
					JidResourceEnum jidResource = JidResourceEnum.fromValue(entry.getKey());
					if (jidResource != null) {
						DomainNodeJid crossDomainInfo = JsonUtils.toBean(entry.getValue(), DomainNodeJid.class);
						// redis.multiGet()有点坑，personId 只能从里面找了...如果redis.multiGet()返回Map就好了...
						if (StringUtils.isBlank(personId) && crossDomainInfo != null
								&& StringUtils.isNotBlank(crossDomainInfo.getPid())) {
							personId = crossDomainInfo.getPid();
						}
						crossDomainInfoMap.put(jidResource, crossDomainInfo);
					}
				}
				if (StringUtils.isNotBlank(personId)) {
					crossDomainInfoMapMap.put(personId, crossDomainInfoMap);
				}
			}
			return crossDomainInfoMapMap;
		} catch (Throwable t) {
			LOG.error("CrossDomainInfoCache.multiGet() error!, personIds: {}", personIds, t);
			return Maps.newHashMap();
		}
	}

	Object getAll() {
		return redisTemplate.boundHashOps(KEY).entries();
	}

	@Override
	public Map<String, Map<JidResourceEnum, DomainNodeJid>> getAllDomain() {
		try {
			Object object = getAll();
			Map<String, String> map = (Map<String, String>) object;
			if (MapUtils.isEmpty(map)) {
				return Maps.newHashMap();
			}
			Map<String, Map<JidResourceEnum, DomainNodeJid>> crossDomainInfoMapMap = Maps.newHashMap();
			for (Map.Entry<String, String> entry : map.entrySet()) {
				if (StringUtils.isBlank(entry.getKey()) || StringUtils.isBlank(entry.getValue())) {
					continue;
				}
				Map<JidResourceEnum, DomainNodeJid> crossDomainInfoMap = Maps.newHashMap();
				Map<String, String> valueMap = JsonUtils.toMap(entry.getValue());
				if (valueMap == null) {
					continue;
				}
				for (Map.Entry<String, String> entry1 : valueMap.entrySet()) {
					if (StringUtils.isBlank(entry1.getKey()) || StringUtils.isBlank(entry1.getValue())) {
						continue;
					}
					JidResourceEnum jidResource = JidResourceEnum.fromValue(entry1.getKey());
					if (jidResource != null) {
						crossDomainInfoMap.put(jidResource, JsonUtils.toBean(entry1.getValue(), DomainNodeJid.class));
					}
				}
				crossDomainInfoMapMap.put(entry.getKey(), crossDomainInfoMap);
			}
			return crossDomainInfoMapMap;
		} catch (Throwable t) {
			LOG.error("CrossDomainInfoCache.getAll() error!", t);
			return Maps.newHashMap();
		}
	}

	Object get(String personId) {
		return redisTemplate.boundHashOps(KEY).get(personId);
	}

	@Override
	public Map<JidResourceEnum, DomainNodeJid> getDomain(String personId) {
		if (StringUtils.isBlank(personId)) {
			return Maps.newHashMap();
		}
		try {
			Object obj = get(personId);
			if (obj == null) {
				return Maps.newHashMap();
			}
			String string = (String) obj;
			if (StringUtils.isBlank(string)) {
				return Maps.newHashMap();
			}
			Map<String, String> map = JsonUtils.toMap(string);
			if (map == null) {
				return Collections.emptyMap();
			}

			Map<JidResourceEnum, DomainNodeJid> domainInfoMap = Maps.newHashMap();
			for (Map.Entry<String, String> entry : map.entrySet()) {
				domainInfoMap.put(JidResourceEnum.fromValue(entry.getKey()),
						JsonUtils.toBean(entry.getValue(), DomainNodeJid.class));
			}
			return domainInfoMap;
		} catch (Throwable t) {
			LOG.error("CrossDomainInfoCache.get() error! personId:{}", personId, t);
			return Maps.newHashMap();
		}
	}

	void put(String key, String value) {
		redisTemplate.boundHashOps(KEY).put(key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void putDomain(DomainNodeJid crossDomainInfo) {
		try {
			if (StringUtils.isNotBlank(crossDomainInfo.getPid())) {
				Object obj = get(crossDomainInfo.getPid());
				String string = (String) obj;
				Map<String, String> crossDomainInfoMap;
				if (obj == null || StringUtils.isBlank(string)) {
					crossDomainInfoMap = Maps.newHashMap();
				} else {
					crossDomainInfoMap = JsonUtils.toMap(obj.toString());
				}
				crossDomainInfoMap.put(crossDomainInfo.getRs(), JsonUtils.fromObject(crossDomainInfo));
				put(crossDomainInfo.getPid(), JsonUtils.fromObject(crossDomainInfoMap));
			}
		} catch (Throwable t) {
			LOG.error("CrossDomainInfoCache.put() error!,crossDomainInfo:{}", JsonUtils.fromObject(crossDomainInfo), t);
		}
	}

	void putAll(Map<String, String> map) {
		redisTemplate.boundHashOps(KEY).putAll(map);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void putAllDomain(List<DomainNodeJid> crossDomainInfoList) {
		try {
			Map<String, String> jsonMap = Maps.newHashMap();
			for (DomainNodeJid crossDomainInfo : crossDomainInfoList) {
				if (StringUtils.isNotBlank(crossDomainInfo.getPid())) {
					// 这里为需要获取用户原有的跨域信息，会频繁调用redis，可能会对redis造成压力
					Object obj = get(crossDomainInfo.getPid());
					String string = (String) obj;
					Map<String, String> crossDomainInfoMap;
					if (obj == null || StringUtils.isBlank(string)) {
						crossDomainInfoMap = Maps.newHashMap();
					} else {
						crossDomainInfoMap = JsonUtils.toMap(string);
					}
					crossDomainInfoMap.put(crossDomainInfo.getRs(), JsonUtils.fromObject(crossDomainInfo));
					jsonMap.put(crossDomainInfo.getPid(), JsonUtils.fromObject(crossDomainInfoMap));
				}
			}
			if (MapUtils.isNotEmpty(jsonMap)) {
				putAll(jsonMap);
			}
		} catch (Throwable t) {
			LOG.error("CrossDomainInfoCache.putAll() error!", t);
		}
	}

}
