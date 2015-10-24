package org.jivesoftware.openfire.plugin.xroster.extcontacts;

import java.io.File;

import org.jivesoftware.of.common.utils.JidUtil;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.component.ExtContactComponent;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.component.ExtContactProtocalEnum;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.constants.ExtContactConstants;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;

public class ExtContactPlugin implements Plugin {

	private final Logger LOG = LoggerFactory.getLogger(ExtContactPlugin.class);
	private String subdomain;
	private XMPPServer xmppServer;
	private ComponentManager componentManager;

	/**
	 * <ol>
	 * <li>disabl eOpened Session In DB</li>
	 * <li>register component item as service discover content</li>
	 * <li>regiter ExternalContactComponent</li>
	 * <li>regiter ChatMsgInterceptor</li>
	 * <li>register RresenceEventListener</li>
	 * </ol>
	 */
	@Override
	public void initializePlugin(PluginManager pluginManager, File file) {

		subdomain = JiveGlobals.getProperty(ExtContactConstants.COMP_DISO_KEY, ExtContactConstants.COMP_SUBDOMAIN);
		xmppServer = XMPPServer.getInstance();
		componentManager = ComponentManagerFactory.getComponentManager();
		ExtContactComponent component = new ExtContactComponent();

		xmppServer.getIQDiscoInfoHandler().addServerFeature(ExtContactProtocalEnum.IQ_EC_URI.getUri());
		xmppServer.getIQDiscoItemsHandler().addComponentItem(subdomain + "." + JidUtil.DOMAIN, component.getName());

		try {
			componentManager.addComponent(subdomain, component);
		} catch (ComponentException ce) {
			LOG.error("add ExternalContactComponent error!", ce);
		}

		System.out.println(this.getClass().getSimpleName() +" ok ..");
		LOG.info(this.getClass().getSimpleName() + " init...");

	}

	/**
	 * <ol>
	 * <li>remove component item as service discover content</li>
	 * <li>remove ExternalContactComponent</li>
	 * <li>remove ChatMsgInterceptor</li>
	 * <li>remove RresenceEventListener</li>
	 * </ol>
	 */
	@Override
	public void destroyPlugin() {

		xmppServer.getIQDiscoInfoHandler().removeServerFeature(ExtContactProtocalEnum.IQ_EC_URI.getUri());
		xmppServer.getIQDiscoItemsHandler().removeComponentItem(subdomain + "." + JidUtil.DOMAIN);

		try {
			componentManager.removeComponent(subdomain);
		} catch (ComponentException ce) {
			LOG.error("remove ExternalContactComponent error!", ce);
		}

		subdomain = null;
		xmppServer = null;
		componentManager = null;

		LOG.info(this.getClass().getSimpleName() + " destroy...");

	}

}
