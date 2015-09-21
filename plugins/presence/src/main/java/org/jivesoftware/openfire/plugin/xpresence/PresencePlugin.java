package org.jivesoftware.openfire.plugin.xpresence;

import java.io.File;

import org.jivesoftware.of.common.plugin.PluginAdaptor;
import org.jivesoftware.of.common.service.RestService;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.util.Encryptor;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PresencePlugin extends PluginAdaptor implements Plugin {
	private Logger logger = LoggerFactory.getLogger(PresencePlugin.class);
	private InterceptorManager interceptorManager;
	private PresencePacketInterceptor interceptor = new PresencePacketInterceptor();

	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		interceptorManager = InterceptorManager.getInstance();
		interceptorManager.addInterceptor(interceptor);
	}

	public void destroyPlugin() {
		interceptorManager.removeInterceptor(interceptor);
	}


	private Encryptor getEncryptor() {
		return JiveGlobals.getPropertyEncryptor();
	}

	@Override
	public boolean register(String serviceName, RestService service) {
		return false;
	}

	@Override
	public boolean unregister() {
		return false;
	}

}
