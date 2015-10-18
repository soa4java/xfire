package com.servyou.openfire.plugin.receipt.handler;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jivesoftware.of.common.version.VersionSetEvent;
import org.jivesoftware.of.common.version.VersionSupport;
import org.jivesoftware.of.common.version.Versions;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.JID;

/**
 * IQ Timer Handler
 * 
 * @author yanricheng@163.com
 * 
 */
public class IQVersionHandler extends IQHandler {

	protected static final String QUERY = "query";

	protected static final String XMLNS = "http://www.servyou.com.cn/protocol/client/version";

	protected IQHandlerInfo info;

	/**
	 * default constructor
	 */
	public IQVersionHandler() {
		super("servyou version handler");
		info = new IQHandlerInfo(QUERY, XMLNS);
	}

	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		IQ response = IQ.createResultIQ(packet);
		response.setTo(packet.getFrom());
		response.setType(Type.result);
		Element query = packet.getChildElement();
		Element versionEle = query.element(Versions.VERSION);
        String version = null;
		if (versionEle != null) {
			version = versionEle.getText();
			JID fromJid = packet.getFrom();
			if (fromJid != null) {
				ClientSession clientSession = XMPPServer.getInstance().getSessionManager().getSession(fromJid);
				if (clientSession instanceof LocalClientSession) {
					LocalClientSession localClientSession = (LocalClientSession) clientSession;
					localClientSession.setSessionData(Versions.VERSION, version);
				}
			}
		}

        VersionSetEvent event = null;
        if(StringUtils.isBlank(version)){
            event = new VersionSetEvent(VersionSupport.NO_VERSION, packet.getFrom());
        }else{
            event = new VersionSetEvent(version, packet.getFrom());
        }
        VersionSupport.OnSet(event);

		return response;
	}

	@Override
	public IQHandlerInfo getInfo() {
		return info;
	}

}
