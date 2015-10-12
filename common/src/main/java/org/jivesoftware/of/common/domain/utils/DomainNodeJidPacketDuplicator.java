package org.jivesoftware.of.common.domain.utils;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.of.common.domain.DomainNodeJid;
import org.jivesoftware.of.common.utils.JidUtil;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.JiveProperties;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Presence;

/**
 * 跨域通信包中继器
 *
 * @author Administrator
 *
 */
public class DomainNodeJidPacketDuplicator {
	public static final String CROSS_DOMAIN_ENABLE_KEY = "cross.domain.enable";
	final static Logger LOG = LoggerFactory.getLogger(DomainNodeJidPacketDuplicator.class);
	public static Boolean crossDomainEnable;
	private volatile static DomainNodeJidPacketDuplicator INSTANCE;

	static {
		crossDomainEnable = JiveGlobals.getBooleanProperty(CROSS_DOMAIN_ENABLE_KEY, false);
		PropertyEventDispatcher.addListener(new PropertyListener());
	}

	private PacketRouter router;
	private String localDomainName;
	private XMPPServer server;

	//private CrossDomainPacketAdaptor crossDomainPacketAdaptor;

	private DomainNodeJidPacketDuplicator() {
		server = XMPPServer.getInstance();
		router = server.getPacketRouter();
		localDomainName = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
		//crossDomainPacketAdaptor = CrossDomainPacketAdaptor.getInstance();
	}

	public static DomainNodeJidPacketDuplicator getInstance() {
		if (INSTANCE == null) {
			synchronized (DomainNodeJidPacketDuplicator.class) {
				if (INSTANCE == null) {
					INSTANCE = new DomainNodeJidPacketDuplicator();
				}
			}
		}
		return INSTANCE;
	}

	public void discardIQIfNeed(IQ iq) {
		if (iq.getChildElement() != null && crossDomainEnable //启用了跨域的
				&& null != iq.getFrom() && null != iq.getTo() //to和from都不为空的
				&& !iq.getFrom().getDomain().endsWith(localDomainName)//跨域发过来的
				&& !iq.getFrom().getDomain().endsWith(iq.getTo().getDomain())//from与to的domain不一样的
				&& (IQ.Type.error == iq.getType() /*|| IQ.Type.result == iq.getType()*/)//error或者result类型的
		) {
			iq.getChildElement().addElement(XConstants.NO_ROUTE, XConstants.NO_ROUTE_HINTS);
		}

		//本域发给其他域的IQ，且对方域返回一个error的。过滤掉
		if (IQ.Type.error == iq.getType() && null != iq.getFrom() && null != iq.getTo() && null != iq.getChildElement()
				&& !StringUtils.equals(JidUtil.getDomain(iq.getTo()), JidUtil.getDomain(iq.getFrom()))) {
			iq.getChildElement().addElement(XConstants.NO_ROUTE, XConstants.NO_ROUTE_HINTS);
		}
	}

	/**
	 * 单聊消息跨域 to 修改
	 *
	 * @param message
	 * @return
	 */
	public void replaceJidDomainForCrossDomainP2PChat(Message message) {
		//不启用跨域就不做处理
		if (!crossDomainEnable) {
			return;
		}

		JID to = message.getTo();
		JID from = message.getFrom();

		//是跨域过来的也不处理
		if (!StringUtils.equals(from.getDomain(), to.getDomain())) {
			return;
		}

		//接收者登录在本地的不处理
		Session session = SessionManager.getInstance().getSession(message.getTo());
		if (session != null) {
			return;
		}

		DomainNodeJid domainInfo = DomainNodeJidCacheUtils.get(to.getNode());
		if (null != domainInfo) {
			message.setTo(new JID(to.getNode(), domainInfo.getDm(), null, true));
		}

		// 不必适配协议了
		/*if (!StringUtils.equals(from.getDomain(), message.getTo().getDomain())) {//跨域才需要适配 add by yanrc
		    crossDomainPacketAdaptor.adapt(message);
		}*/

	}

	/**
	 * 出席信息跨域拷贝发送
	 *
	 * @param presence
	 * @return
	 */
	public Presence duplicate(Presence presence) {

		//不启用跨域就不做处理
		if (!crossDomainEnable || presence.getFrom() == null) {
			return null;
		}

		Session session = SessionManager.getInstance().getSession(presence.getFrom());
		if (session != null && session.getStatus() != Session.STATUS_AUTHENTICATED) {
			return null;
		}

		JID fromJID = presence.getFrom();
		// 发送者的domain信息和当前服务器名称相同，说明是本地客户端发送的Presence，需要转发到远程域服务器
		if (StringUtils.equals(localDomainName, fromJID.getDomain())) {

			String crossDomain = JiveProperties.getInstance().getProperty("cross.domains", "");
			if (StringUtils.isNotBlank(crossDomain)) {
				String[] crossDomains = crossDomain.split(",");
				for (String domain : crossDomains) {
					if (!domain.equals(localDomainName)) {
						Presence presenceCopy = presence.createCopy();
						// 设置to jid为远程domain
						presenceCopy.setTo(new JID(null, domain, null));
						// 协议不必适配了
						// crossDomainPacketAdaptor.adapt(presenceCopy);

						return presenceCopy;
					}
				}
			}
		}
		return null;
	}

	private static class PropertyListener implements PropertyEventListener {
		public void propertySet(String property, Map<String, Object> params) {
			if (CROSS_DOMAIN_ENABLE_KEY.equals(property)) {
				String value = (String) params.get("value");
				if (value != null) {
					crossDomainEnable = Boolean.parseBoolean(value);
				}
			}

		}

		public void propertyDeleted(String property, Map<String, Object> params) {
			if (CROSS_DOMAIN_ENABLE_KEY.equals(property)) {
				crossDomainEnable = false;
			}
		}

		public void xmlPropertySet(String property, Map<String, Object> params) {
			// Do nothing
		}

		public void xmlPropertyDeleted(String property, Map<String, Object> params) {
			// Do nothing
		}
	}
}
