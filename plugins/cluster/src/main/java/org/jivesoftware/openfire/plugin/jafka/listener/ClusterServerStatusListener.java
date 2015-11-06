package org.jivesoftware.openfire.plugin.jafka.listener;

import org.jivesoftware.of.common.node.cache.NodeCache;
import org.jivesoftware.of.common.node.cache.impl.redis.RedisNodeCacheImpl;
import org.jivesoftware.openfire.XMPPServerListener;
import org.jivesoftware.openfire.plugin.jafka.ClusterPlugin;

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
