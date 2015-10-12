package org.jivesoftware.of.common.domain.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.jivesoftware.of.common.domain.DomainNodeJid;
import org.jivesoftware.of.common.domain.DomainNodeJidCache;
import org.jivesoftware.of.common.enums.JidResourceEnum;
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
    public static ConcurrentMap<String, Map<JidResourceEnum, DomainNodeJid>> localCrossDomainInfoMap = Maps.newConcurrentMap();
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
    public void putDomain(DomainNodeJid crossDomainInfo) {
        String personId = crossDomainInfo.getPid();
        if (StringUtils.isNotBlank(personId)) {
            Map<JidResourceEnum, DomainNodeJid> crossDomainInfoMap = localCrossDomainInfoMap.get(personId);
            if (crossDomainInfoMap == null) {
                crossDomainInfoMap = Maps.newConcurrentMap(); //同步map
                Map<JidResourceEnum, DomainNodeJid> old = localCrossDomainInfoMap.putIfAbsent(personId, crossDomainInfoMap);
                if (old != null) {
                    //如已存在，使用老的
                    crossDomainInfoMap = old;
                }
            }
            String resource = crossDomainInfo.getRs();
            if (StringUtils.isNotBlank(resource)) {
                crossDomainInfoMap.put(JidResourceEnum.fromValue(resource), crossDomainInfo);
            }
        }
    }

    @Override
    public Map<String, Map<JidResourceEnum, DomainNodeJid>> getAllDomain() {
        return localCrossDomainInfoMap;
    }

    @Override
    public void putAllDomain(List<DomainNodeJid> crossDomainInfoList) {
        for (DomainNodeJid crossDomainInfo : crossDomainInfoList) {
            putDomain(crossDomainInfo);
        }
    }

    @Override
    public Map<String, DomainNodeJid> multiFetchDomain(Collection<Object> personIds) {
        Map<String, DomainNodeJid> crossDomainInfoMap = Maps.newHashMap();
        for (Object object : personIds) {
            String personId = (String) object;
            if (StringUtils.isNotBlank(personId)) {
                Map<JidResourceEnum, DomainNodeJid> paramMap = localCrossDomainInfoMap.get(personId);
                if (MapUtils.isNotEmpty(paramMap)) {
                    List<DomainNodeJid> crossDomainMap = Lists.newArrayList();
                    crossDomainMap.addAll(paramMap.values());
                    Collections.sort(crossDomainMap);
                    DomainNodeJid crossDomainInfo = crossDomainMap.get(crossDomainMap.size() - 1);
                   
                    if (crossDomainInfo != null) {
                        crossDomainInfoMap.put(personId, crossDomainInfo);
                    }
                }
            }
        }
        return crossDomainInfoMap;
    }

    @Override
    public Map<String, Map<JidResourceEnum, DomainNodeJid>> multiGetDomain(Collection<Object> personIds) {
        Map<String, Map<JidResourceEnum, DomainNodeJid>> crossDomainInfoMapMap = Maps.newHashMap();
        for (Object object : personIds) {
            String personId = (String) object;
            if (StringUtils.isNotBlank(personId)) {
                Map<JidResourceEnum, DomainNodeJid> crossDomainInfoMap = localCrossDomainInfoMap.get(personId);
                if (crossDomainInfoMap != null) {
                    crossDomainInfoMapMap.put(personId, crossDomainInfoMap);
                }
            }
        }
        return crossDomainInfoMapMap;
    }

    @Override
    public Map<JidResourceEnum, DomainNodeJid> getDomain(String personId) {
        Map<JidResourceEnum, DomainNodeJid> crossDomainInfoMap = localCrossDomainInfoMap.get(personId);
        if (crossDomainInfoMap == null) {
            return Maps.newHashMap();
        }
        return crossDomainInfoMap;
    }
}
