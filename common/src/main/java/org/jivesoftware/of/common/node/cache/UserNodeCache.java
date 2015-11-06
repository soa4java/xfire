package org.jivesoftware.of.common.node.cache;

import java.util.List;

import org.jivesoftware.of.common.node.UserNode;

public interface UserNodeCache {

	List<UserNode> get(String key);

	void put(String key, UserNode value);

	void remove(String key, UserNode value);

}
