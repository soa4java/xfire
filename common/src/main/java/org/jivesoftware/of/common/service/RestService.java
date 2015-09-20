package org.jivesoftware.of.common.service;

import java.util.Map;

public interface RestService {
	
	@SuppressWarnings("rawtypes")
	Map execute(Map params);

}
