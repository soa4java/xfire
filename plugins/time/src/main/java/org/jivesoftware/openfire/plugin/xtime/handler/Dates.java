package org.jivesoftware.openfire.plugin.xtime.handler;

import org.apache.commons.lang.time.DateFormatUtils;
import org.dom4j.Element;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.XMPPDateTimeFormat;
import org.xmpp.packet.Message;

public abstract class Dates {

    public static final String FORMAT_KEY = "timestamp.format";
    
     static final String TIME_STAMP = "timestamp";

    public static void addTimetamp(Message packet) {
        if (packet != null && packet.getElement() != null) {
            Element createDateEle = packet.getElement().element(TIME_STAMP);
            if (null == createDateEle) {
                String dateFormat = JiveGlobals.getProperty(FORMAT_KEY, XMPPDateTimeFormat.XMPP_DATETIME_FORMAT);
                long currentTimeMillis = System.currentTimeMillis();
                createDateEle = packet.getElement().addElement(TIME_STAMP);
                createDateEle.setText(String.valueOf(currentTimeMillis));
                createDateEle.addAttribute("stamp", DateFormatUtils.format(currentTimeMillis, dateFormat));
            }
        }
    }
}