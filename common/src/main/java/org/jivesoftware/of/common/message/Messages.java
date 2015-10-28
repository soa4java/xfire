package org.jivesoftware.of.common.message;

import org.apache.commons.lang.StringUtils;
import org.xmpp.packet.Message;

public abstract class Messages {
	
	 public static  boolean isControlMessage(Message message){
	    	if(message.getType() == Message.Type.normal ||  message.getType()==null || StringUtils.isBlank(message.getType().toString())){
	    		return true;
	    	}
	    	return false;
	    }

}
