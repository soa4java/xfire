package org.jivesoftware.openfire.plugin.xroster.internal.handler;

import org.dom4j.Element;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

public class IQMystatusHandler extends IQHandler {

	Logger logger = LoggerFactory.getLogger(IQMystatusHandler.class);

	protected static final String QUERY = "query";

	protected static final String XMLNS = "http://www.servyou.com.cn/protocol/mystatus";

	protected IQHandlerInfo info;

	/**
	 * default constructor
	 */
	public IQMystatusHandler() {
		super("servyou mystatus handler");
		info = new IQHandlerInfo(QUERY, XMLNS);
	}

	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		IQ response = IQ.createResultIQ(packet);
		response.setChildElement(packet.getChildElement().createCopy());

		appendPresenceEle(response.getChildElement(), packet.getFrom());

		if (logger.isInfoEnabled()) {
			logger.info("reqId:{}获取我的状态response:{}", packet.getID(), response.toXML());
		}

		return response;
	}

	public void appendPresenceEle(Element parentEle, JID jid) {

		try {

			Presence presence = null;

			if (jid != null) {
				ClientSession clientSession = XMPPServer.getInstance().getSessionManager().getSession(jid);
				if (clientSession == null || clientSession.getAddress() == null) {
					return;
				}
				presence = clientSession.getPresence();
			}

			if (presence == null) {
				return;
			}

			String status = presence.getStatus();
			parentEle.addElement("status").setText(null != status ? status : "");

		} catch (Throwable t) {
			logger.error("IQMystatusHandler error!", t);
		}
	}

	@Override
	public IQHandlerInfo getInfo() {
		return info;
	}

}
