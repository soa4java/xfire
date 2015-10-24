package org.jivesoftware.openfire.plugin.xfileserver;

import java.io.File;
import java.util.Map;

import org.dom4j.Element;
import org.jivesoftware.of.common.service.RestService;
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
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;

public class FileServerPlugin implements Plugin, Component {

	private static final Logger LOG = LoggerFactory.getLogger(FileServerPlugin.class);
	private PluginManager pluginManager;
	private ComponentManager componentManager;
	private String serviceName;

	public Map<String, RestService> getRestServiceMap() {
		return null;
	}

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		serviceName = JiveGlobals.getProperty("plugin.filetransfer.serviceName", "filetransfer");
		pluginManager = manager;
		componentManager = ComponentManagerFactory.getComponentManager();
		try {
			componentManager.addComponent(serviceName, this);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		String domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
		XMPPServer.getInstance().getIQDiscoItemsHandler().addComponentItem(serviceName + "." + domain, getName());
	}

	@Override
	public void destroyPlugin() {
		if (componentManager != null) {
			try {
				componentManager.removeComponent(serviceName);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}

		XMPPServer.getInstance().getIQDiscoItemsHandler().removeComponentItem(serviceName);
		componentManager = null;
		pluginManager = null;

	}

	@Override
	public String getDescription() {
		return pluginManager.getDescription(this);
	}

	@Override
	public String getName() {
		return pluginManager.getName(this);
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@Override
	public void initialize(JID arg0, ComponentManager arg1) throws ComponentException {
		// TODO Auto-generated method stub
		LOG.info("component fileTransfer initialized.");
	}

	@Override
	public void processPacket(Packet packet) {
		if (packet instanceof Message) {
			// Respond to incoming messages
			Message message = (Message) packet;
			processMessage(message);
		} else if (packet instanceof Presence) {
			// Respond to presence subscription request or presence probe
			Presence presence = (Presence) packet;
			processPresence(presence);
		} else if (packet instanceof IQ) {
			// Handle disco packets
			IQ iq = (IQ) packet;
			// Ignore IQs of type ERROR or RESULT
			if (IQ.Type.error == iq.getType() || IQ.Type.result == iq.getType()) {
				return;
			}
			processIQ(iq);
		}

	}

	private void processMessage(Message message) {

	}

	private void processPresence(Presence presence) {

	}

	private void processIQ(IQ iq) {
		IQ reply = IQ.createResultIQ(iq);
		Element childElement = iq.getChildElement();
		String namespace = childElement.getNamespaceURI();
		Element childElementCopy = iq.getChildElement().createCopy();
		reply.setChildElement(childElementCopy);
		if ("http://jabber.org/protocol/disco#info".equals(namespace)) {
			// Return service identity and features
			Element identity = childElementCopy.addElement("identity");
			identity.addAttribute("category", "component");
			identity.addAttribute("type", "generic");
			identity.addAttribute("name", "fileTransfer service");
			childElementCopy.addElement("feature").addAttribute("var", "http://jabber.org/protocol/disco#info");
			childElementCopy.addElement("feature").addAttribute("var",
					"http://www.servyou.com.cn/protocol/filetransfer");

		} else if ("http://www.servyou.com.cn/protocol/filetransfer".equals(namespace)) {

			if (iq.getType() == IQ.Type.get) {
				//获取总的文件服务器列表
				String hoststr = JiveGlobals.getProperty("plugin.filetransfer.hosts", "").trim();
				String[] hosts = hoststr.split(";");
				Element hostsEle = childElementCopy.addElement("transferhosts");
				for (String host : hosts) {
					if ("".equals(host.trim())) {
						continue;
					}
					String ip = JiveGlobals.getProperty(host + ".ip", "").trim();
					String port = JiveGlobals.getProperty(host + ".port", "").trim();
					String type = JiveGlobals.getProperty(host + ".type", "").trim();
					String netType = JiveGlobals.getProperty(host + ".nettype", "").trim();
					String app = JiveGlobals.getProperty(host + ".app", "").trim();
					String imageUploadUrl = JiveGlobals.getProperty(host + ".image.upload.url", "/api/img/upload")
							.trim();
					if ("".equals(ip) || "".equals(port)) {
						continue;
					}
					Element hostudt = hostsEle.addElement("transferhost");
					hostudt.addAttribute("ip", ip);
					hostudt.addAttribute("port", port);
					hostudt.addAttribute("type", type);
					hostudt.addAttribute("netType", netType);
					hostudt.addAttribute("app", app);
					hostudt.addAttribute("imageUploadUrl", imageUploadUrl);
				}
				// System.out.println(LdapInteralRosterManager.getInstance().sb.toString());
			} else {
				reply = IQ.createResultIQ(iq);
				reply.setChildElement(iq.getChildElement().createCopy());
				reply.setError(PacketError.Condition.feature_not_implemented);
			}
		}

		else {
			reply = IQ.createResultIQ(iq);
			reply.setChildElement(iq.getChildElement().createCopy());
			reply.setError(PacketError.Condition.service_unavailable);
		}
		try {

			if (LOG.isInfoEnabled()) {
				LOG.info("FileTransferPlugin 文件服务请求  request: {},response:{}", iq.toXML(), reply.toXML());
			}

			componentManager.sendPacket(this, reply);
		} catch (Exception e) {
			LOG.error("FileTransferPlugin 文件服务请求异常 request: {},response:{}", iq.toXML(), reply.toXML(), e);
		}

	}

	@Override
	public void shutdown() {

	}

	@Override
	public void start() {

		System.out.println("fileserver ok ...");
	}

}
