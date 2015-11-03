package org.jivesoftware.openfire.plugin.jafka.cache.impl.redis;

import net.yanrc.app.common.util.JsonUtils;

import org.jivesoftware.of.common.spring.SpringContextHolder;
import org.jivesoftware.openfire.plugin.jafka.cache.NodeCache;
import org.jivesoftware.openfire.plugin.jafka.vo.ImNode;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisNodeCacheImpl implements NodeCache {

	private final String KEY = "hash_nodes";

	private RedisTemplate<String, String> redisTemplate;

	private static NodeCache instance;

	public static NodeCache getInstance() {
		if (instance == null) {
			synchronized (RedisNodeCacheImpl.class) {
				if (instance == null) {
					instance = new RedisNodeCacheImpl();
				}
			}
		}

		return instance;
	}

	@SuppressWarnings("unchecked")
	private RedisNodeCacheImpl() {
		redisTemplate = SpringContextHolder.getBean(RedisTemplate.class);
	}

	@Override
	public ImNode get(String key) {
		Object obj = redisTemplate.boundHashOps(KEY).get(key);

		if (obj != null) {
			return JsonUtils.toBean(obj.toString(), ImNode.class);
		}
		return null;
	}

	@Override
	public void put(String key, ImNode value) {
		if (key!=null && value != null) {
			redisTemplate.boundHashOps(KEY).put(key, JsonUtils.fromObject(value));
		}
	}
	
	@Override
	public void delete(String key) {
		if (key != null) {
			redisTemplate.boundHashOps(KEY).delete(key);
		}
	}

}
