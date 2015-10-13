package org.jivesoftware.openfire.plugin.xtime.handler;

import org.apache.commons.lang.time.DateFormatUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.XMPPDateTimeFormat;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;

/**
 * IQ Timer Handler
 *
 * @author yanricheng@163.com
 */
public class IQTimerHandler extends IQHandler {

	protected static final String QUERY = "query";
	protected static final String XMLNS = "http://www.servyou.com.cn/protocol/timer";
	protected IQHandlerInfo info;

	/**
	 * default constructor
	 */
	public IQTimerHandler() {
		super("timer handler");
		info = new IQHandlerInfo(QUERY, XMLNS);
	}

	private static String getCurrentDateString(long millisTime) {
		String dateFormat = JiveGlobals.getProperty(Dates.FORMAT_KEY, XMPPDateTimeFormat.XMPP_DATETIME_FORMAT);
		return DateFormatUtils.format(millisTime, dateFormat);
	}

	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		IQ response = IQ.createResultIQ(packet);
		response.setTo(packet.getFrom());
		response.setType(Type.result);
		Element query = DocumentHelper.createElement(QName.get(QUERY, XMLNS));
		long millisTime = System.currentTimeMillis();
		query.addElement("timestamp").setText(String.valueOf(millisTime));
		query.addElement("time").setText(getCurrentDateString(millisTime));
		response.setChildElement(query);
		return response;
	}

	@Override
	public IQHandlerInfo getInfo() {
		return info;
	}

}
