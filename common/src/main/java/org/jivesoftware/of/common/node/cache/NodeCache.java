package org.jivesoftware.of.common.node.cache;

import org.jivesoftware.of.common.node.ImNode;

public interface NodeCache {
	ImNode get(String key);

	void put(String key, ImNode value);
	
	void delete(String key);

}
