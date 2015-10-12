package sitong.thinker.of.plugin.groupchat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.of.common.service.RestService;
import org.jivesoftware.of.common.utils.JidUtil;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;

import sitong.thinker.of.plugin.groupchat.component.GroupChatComponent;
import sitong.thinker.of.plugin.groupchat.enums.ProtocolEnum;

/**
 * group chat plugin
 *
 * @author yanricheng@163.com
 */
public class GroupChatPlugin implements Plugin {
	final Logger LOG = LoggerFactory.getLogger(GroupChatPlugin.class);

	Map<String, RestService> apiMap = new HashMap<String, RestService>();

	private String subDomain;
	private XMPPServer xmppServer;
	private Component groupChatComponent;
	private ComponentManager componentManager;

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		groupChatComponent = new GroupChatComponent();
		subDomain = JiveGlobals.getProperty("servyou.groupchat", "groupchat");
		xmppServer = XMPPServer.getInstance();
		componentManager = ComponentManagerFactory.getComponentManager();

		xmppServer.getIQDiscoInfoHandler().addServerFeature(ProtocolEnum.IQ_GROUP_CHAT.getUrl());
		String componentItemName = subDomain + "." + JidUtil.DOMAIN;
		xmppServer.getIQDiscoItemsHandler().addComponentItem(componentItemName, groupChatComponent.getName());

		try {
			componentManager.addComponent(subDomain, groupChatComponent);
		} catch (ComponentException ce) {
			LOG.error("insert ExternalContactComponent error!", ce);
			return;
		}

		System.out.println("groupChat init succeed ...");
	}

	@Override
	public void destroyPlugin() {

		xmppServer.getIQDiscoInfoHandler().removeServerFeature(ProtocolEnum.IQ_GROUP_CHAT.getUrl());
		xmppServer.getIQDiscoItemsHandler().removeComponentItem(subDomain + "." + JidUtil.DOMAIN);

		try {
			componentManager.removeComponent(subDomain);
		} catch (ComponentException ce) {
			LOG.error("delete groupChatComponent error!", ce);
			return;
		}

		System.out.println("GroupChatPlugin destroy succeed!");

		if (LOG.isInfoEnabled()) {
			LOG.info("GroupChatPlugin destroy succeed!");
		}
	}

	/**
	 * 获取所有向插件注册api
	 *
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, RestService> getRestServiceMap() {
		return apiMap;
	}

}
