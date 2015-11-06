package org.jivesoftware.openfire.plugin.jafka.cache;

import org.jivesoftware.openfire.plugin.jafka.vo.ImNode;

public interface NodeCache {
	ImNode get(String key);

	void put(String key, ImNode value);
	
	void delete(String key);

}
