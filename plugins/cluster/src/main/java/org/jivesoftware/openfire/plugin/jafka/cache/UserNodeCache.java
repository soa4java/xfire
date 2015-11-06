package org.jivesoftware.openfire.plugin.jafka.cache;

import java.util.List;

import org.jivesoftware.openfire.plugin.jafka.vo.UserNode;

public interface UserNodeCache {

	List<UserNode> get(String key);

	void put(String key, UserNode value);

	void remove(String key, UserNode value);

}
