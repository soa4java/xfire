package org.jivesoftware.of.common.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jivesoftware.of.common.enums.Resource;

public interface DomainNodeJidCache {
	
	public Map<String, UserTicket> multiFetchDomain(Collection<Object> personIds);

	/**
	 * 同时获取多个用户跨域信息
	 * @param personIds
	 * @return Map<personId, Map<Resource, CrossDomainInfo>>
	 */
	public Map<String, Map<Resource, UserTicket>> multiGetDomain(Collection<Object> personIds);

	/**
	 * 获取某个用户跨域信息Map
	 * @param personId
	 * @return Map<资源, 跨域信息>
	 */
	public Map<Resource, UserTicket> getDomain(String personId);

	/**
	 * 缓存某个用户的跨域信息
	 * @param crossDomainInfo
	 */
	public void putDomain(UserTicket crossDomainInfo);

    /**
     * 获取所有用户的跨域信息
     * return Map<personId, map<resource, crossDomainInfo>>
     */
    public Map<String, Map<Resource, UserTicket>> getAllDomain();

    /**
     * 缓存一批用户的跨域信息
     * @param crossDomainInfoList
     */
    public void putAllDomain(List<UserTicket> crossDomainInfoList);

}
