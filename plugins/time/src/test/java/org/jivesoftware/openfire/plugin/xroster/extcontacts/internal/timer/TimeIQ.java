package org.jivesoftware.openfire.plugin.xroster.extcontacts.internal.timer;

import org.jivesoftware.smack.packet.IQ;

public class TimeIQ extends IQ {

    @Override
    public String getChildElementXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<query xmlns=\"http://www.servyou.com/protocol/timer\">");
        sb.append("</query>");
        return sb.toString(); 
    }

}
