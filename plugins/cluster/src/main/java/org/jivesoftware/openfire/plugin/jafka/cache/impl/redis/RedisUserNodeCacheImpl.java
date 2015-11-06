package org.jivesoftware.openfire.plugin.jafka.cache.impl.redis;

import java.util.ArrayList;
import java.util.List;

import net.yanrc.app.common.util.JsonUtils;

import org.apache.commons.collections.CollectionUtils;
import org.jivesoftware.of.common.spring.SpringContextHolder;
import org.jivesoftware.openfire.plugin.jafka.cache.UserNodeCache;
import org.jivesoftware.openfire.plugin.jafka.vo.UserNode;
import org.springframework.data.redis.core.RedisTemplate;

import com.google.common.collect.Lists;

public class RedisUserNodeCacheImpl implements UserNodeCache {

	private final String KEY = "hash_usernodes";

	private RedisTemplate<String, String> redisTemplate;

	private static UserNodeCache instance;

	public static UserNodeCache getInstance() {
		if (instance == null) {
			synchronized (RedisUserNodeCacheImpl.class) {
				if (instance == null) {
					instance = new RedisUserNodeCacheImpl();
				}
			}
		}

		return instance;
	}

	@SuppressWarnings("unchecked")
	private RedisUserNodeCacheImpl() {
		redisTemplate = SpringContextHolder.getBean(RedisTemplate.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserNode> get(String key) {
		Object obj = redisTemplate.boundHashOps(KEY).get(key);

		if (obj != null) {
			return JsonUtils.toBean(obj.toString(), JsonUtils.createCollectionType(List.class,UserNode.class));
		}
		return new ArrayList<UserNode>(0); 
	}

	@Override
	public void put(String key, UserNode value) {

		List<UserNode> list = get(key);
		if (CollectionUtils.isNotEmpty(list)) {

			List<UserNode> target = new ArrayList<UserNode>();

			for (UserNode node : list) {
				if (node.getPid().equalsIgnoreCase(key) && !node.getResource().equalsIgnoreCase(value.getResource())) {
					target.add(node);
				}
			}

			if (CollectionUtils.isNotEmpty(target)) {
				list.removeAll(target);
				list.add(value);
				redisTemplate.boundHashOps(KEY).put(key, JsonUtils.fromObject(list));
			}

		} else {
			list = new ArrayList<UserNode>();
			list.add(value);
			redisTemplate.boundHashOps(KEY).put(key, JsonUtils.fromObject(list));
		}
	}

	@Override
	public void remove(String key, UserNode value) {

		List<UserNode> list = get(key);
		if (CollectionUtils.isNotEmpty(list)) {
			List<UserNode> target = new ArrayList<UserNode>();
			for (UserNode node : list) {
				if (node.getPid().equalsIgnoreCase(key) && node.getResource().equalsIgnoreCase(value.getResource())) {
					target.add(node);
				}
			}

			list.removeAll(target);
		}

		if (value != null) {
			redisTemplate.boundHashOps(KEY).put(key, JsonUtils.fromObject(list));
		}
	}

}
