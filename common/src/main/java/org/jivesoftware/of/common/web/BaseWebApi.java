package org.jivesoftware.of.common.web;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.of.common.service.RestService;

public class BaseWebApi {
	private final static Map  MAP_ = new HashMap();
	
	public static RestService getService(String serviceName){
		return (RestService)MAP_.get(serviceName);
	}

}
