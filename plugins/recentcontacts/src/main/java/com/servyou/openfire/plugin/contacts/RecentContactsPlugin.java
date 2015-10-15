package com.servyou.openfire.plugin.contacts;

import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.servyou.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.servyou.openfire.plugin.contacts.interceptor.RecentContactsMsgInterceptor;

import java.io.File;
import java.util.Map;

/**
 * @author yanrc
 * @version 1.0, 2015/06/10
 */
public class RecentContactsPlugin implements Plugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecentContactsPlugin.class);
    private PacketInterceptor recentContactsMsgInterceptor = new RecentContactsMsgInterceptor();
    private InterceptorManager interceptorManager = InterceptorManager.getInstance();

    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        interceptorManager.addInterceptor(recentContactsMsgInterceptor);
        System.out.println(RecentContactsPlugin.class.getSimpleName() + " inited...");
    }

    public void destroyPlugin() {
        interceptorManager.removeInterceptor(recentContactsMsgInterceptor);
        RecentContactsMsgInterceptor.getChatMsgExecutorService().shutdown();
    }

    public Map<String, RestService> getRestServiceMap() {
        return null;
    }


}
