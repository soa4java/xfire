package org.jivesoftware.of.common.domain.impl;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.jivesoftware.of.common.domain.DomainNodeJid;
import org.jivesoftware.of.common.enums.JidResourceEnum;
import org.jivesoftware.of.common.enums.StateEnum;
import org.jivesoftware.openfire.XMPPServer;

/**
 * Created by yanrc on 2015/4/22.
 */
public abstract class AbstractDomainNodeJidCache {

    private static final String LOCAL_DOMAIN = XMPPServer.getInstance().getServerInfo().getXMPPDomain();

    abstract Map<String, Map<JidResourceEnum, DomainNodeJid>> getAllDomain();

    abstract void putDomain(DomainNodeJid crossDomainInfo);

    /**
     * 置空本域的在线状态
     */
    void invalAll() {
        Map<String, Map<JidResourceEnum, DomainNodeJid>> mapMap = getAllDomain();
        if (MapUtils.isNotEmpty(mapMap)) {
            Iterator<Map<JidResourceEnum, DomainNodeJid>> outterItr = mapMap.values().iterator();
            while (outterItr.hasNext()) {
                Map<JidResourceEnum, DomainNodeJid> map = outterItr.next();
                if (MapUtils.isEmpty(map)) {
                    continue;
                }
                Iterator<DomainNodeJid> innerIter = map.values().iterator();
                while (innerIter.hasNext()) {
                    DomainNodeJid domainInfo = innerIter.next();
                    if (LOCAL_DOMAIN.equals(domainInfo.getDm())) {
                        domainInfo.setSs(StateEnum.INVAL.code());
                        putDomain(domainInfo);
                    }
                }
            }
        }
    }
}
