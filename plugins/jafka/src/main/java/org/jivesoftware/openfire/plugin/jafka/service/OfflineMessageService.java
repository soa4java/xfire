package org.jivesoftware.openfire.plugin.jafka.service;

import org.xmpp.packet.Message;

public interface OfflineMessageService {
	
	 void saveOfflineMsg(Message msg);

}
