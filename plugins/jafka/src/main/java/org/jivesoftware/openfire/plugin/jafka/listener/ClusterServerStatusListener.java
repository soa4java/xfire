package org.jivesoftware.openfire.plugin.jafka.listener;

import org.jivesoftware.openfire.XMPPServerListener;
import org.jivesoftware.openfire.plugin.jafka.ClusterPlugin;
import org.jivesoftware.openfire.plugin.jafka.cache.NodeCache;
import org.jivesoftware.openfire.plugin.jafka.cache.impl.redis.RedisNodeCacheImpl;

public class ClusterServerStatusListener implements XMPPServerListener {
	
	private NodeCache  nodeCache;
	public ClusterServerStatusListener(){
		nodeCache = RedisNodeCacheImpl.getInstance();
	}

	@Override
	public void serverStarted() {

	}

	@Override
	public void serverStopping() {
		nodeCache.delete(ClusterPlugin.nodeName);
	}

}
