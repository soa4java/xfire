package org.jivesoftware.of.common.plugin;

import org.jivesoftware.of.common.service.RestService;

public abstract class PluginAdaptor {
	abstract public boolean register(String serviceName, RestService service);

	abstract public boolean unregister();
}
