package org.jivesoftware.of.common.domain.impl;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.jivesoftware.of.common.domain.UserTicket;
import org.jivesoftware.of.common.enums.PresenceStatus;
import org.jivesoftware.of.common.enums.Resource;
import org.jivesoftware.openfire.XMPPServer;

/**
 * Created by yanrc on 2015/4/22.
 */
public abstract class AbstractDomainNodeJidCache {

    private static final String LOCAL_DOMAIN = XMPPServer.getInstance().getServerInfo().getXMPPDomain();

    abstract Map<String, Map<Resource, UserTicket>> getAllDomain();

    abstract void putDomain(UserTicket crossDomainInfo);

    /**
     * 置空本域的在线状态
     */
    void invalAll() {
        Map<String, Map<Resource, UserTicket>> mapMap = getAllDomain();
        if (MapUtils.isNotEmpty(mapMap)) {
            Iterator<Map<Resource, UserTicket>> outterItr = mapMap.values().iterator();
            while (outterItr.hasNext()) {
                Map<Resource, UserTicket> map = outterItr.next();
                if (MapUtils.isEmpty(map)) {
                    continue;
                }
                Iterator<UserTicket> innerIter = map.values().iterator();
                while (innerIter.hasNext()) {
                    UserTicket domainInfo = innerIter.next();
                    if (LOCAL_DOMAIN.equals(domainInfo.getDm())) {
                        domainInfo.setSs(PresenceStatus.INVAL.code());
                        putDomain(domainInfo);
                    }
                }
            }
        }
    }
}
