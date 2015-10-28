package org.jivesoftware.openfire.plugin.xroster.internal.interceptor;

import org.apache.commons.lang.time.DateFormatUtils;
import org.dom4j.Element;
import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.XMPPDateTimeFormat;
import org.xmpp.packet.Message;

 abstract class Timetamps {

	public static final String DATE_FORMAT_KEY = "message.date.format";

	public static void addTimetamp(Message packet) {
		if (packet != null && packet.getElement() != null) {
			Element createDateEle = packet.getElement().element(XConstants.TIME_STAMP);
			if (null == createDateEle) {
				String dateFormat = JiveGlobals.getProperty(DATE_FORMAT_KEY, XMPPDateTimeFormat.XMPP_DATETIME_FORMAT);
				long currentTimeMillis = System.currentTimeMillis();
				createDateEle = packet.getElement().addElement(XConstants.TIME_STAMP);
				createDateEle.setText(String.valueOf(currentTimeMillis));
				createDateEle.addAttribute("stamp", DateFormatUtils.format(currentTimeMillis, dateFormat));
				createDateEle.addAttribute("type", XConstants.MSG_TYPE_REALTIME);
			}
		}
	}
}
