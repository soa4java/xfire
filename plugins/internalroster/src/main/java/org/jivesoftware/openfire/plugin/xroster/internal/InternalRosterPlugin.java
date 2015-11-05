package org.jivesoftware.openfire.plugin.xroster.internal;

import java.io.File;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.event.SessionEventDispatcher;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.plugin.xroster.internal.component.InterRosterComponent;
import org.jivesoftware.openfire.plugin.xroster.internal.handler.IQMystatusHandler;
import org.jivesoftware.openfire.plugin.xroster.internal.interceptor.InternalRosterMessageInterceptor;
import org.jivesoftware.openfire.plugin.xroster.internal.listener.InterRosterPresenceEventListener;
import org.jivesoftware.openfire.plugin.xroster.internal.listener.InterRosterResourceBindListener;
import org.jivesoftware.openfire.user.PresenceEventDispatcher;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;

public class InternalRosterPlugin implements Plugin {

	private static final String DEFAULT_SERVICENAME = "internalroster";

	private static final String SERVICE_NAME = "plugin.internalRoster.serviceName";

	private static final Logger Log = LoggerFactory.getLogger(InternalRosterPlugin.class);

	private ComponentManager componentManager;
	private String serviceName;
	private IQHandler handler;
	private InterRosterResourceBindListener resourceBindListener;
//	private InterRosterPresenceEventListener presenceEventListener;
	private InternalRosterMessageInterceptor messageInterceptor;

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		serviceName = JiveGlobals.getProperty(SERVICE_NAME, DEFAULT_SERVICENAME);

		handler = new IQMystatusHandler();
		resourceBindListener = new InterRosterResourceBindListener();
//		presenceEventListener = new InterRosterPresenceEventListener();
		messageInterceptor = new InternalRosterMessageInterceptor();

		XMPPServer.getInstance().getIQRouter().addHandler(handler);

		try {
			// 注册内部组件
			componentManager = ComponentManagerFactory.getComponentManager();
			componentManager.addComponent(serviceName, new InterRosterComponent());

			SessionEventDispatcher.addListener(resourceBindListener);
//			PresenceEventDispatcher.addListener(presenceEventListener);
			InterceptorManager.getInstance().addInterceptor(messageInterceptor);

			// 将组件注册为服务发现内容
			String domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
			XMPPServer.getInstance().getIQDiscoItemsHandler().addComponentItem(serviceName + "." + domain, serviceName);

			System.out.println("InternalRosterPlugin init successfully ....");

		} catch (Exception e) {
			Log.error("InternalRosterPlugin initializePlugin error:" + e.getMessage(), e);
		}

	}

	@Override
	public void destroyPlugin() {
		try {
			XMPPServer.getInstance().getIQRouter().removeHandler(handler);
			//注销组件的注册
			componentManager.removeComponent(serviceName);
			//注销服务发现的注册
			XMPPServer.getInstance().getIQDiscoItemsHandler().removeComponentItem(serviceName);
			SessionEventDispatcher.removeListener(resourceBindListener);
//			PresenceEventDispatcher.removeListener(presenceEventListener);
			InterceptorManager.getInstance().removeInterceptor(messageInterceptor);

			if (Log.isInfoEnabled()) {
				Log.info("InternalRosterPlugin destroy successfully...");
			}
			
			System.out.println(this.getClass().getSimpleName() +" destroy succeed!");

		} catch (Exception e) {
			Log.error("InternalRosterComponent->destroyPlugin:" + e.getMessage(), e);
		}

	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

}
