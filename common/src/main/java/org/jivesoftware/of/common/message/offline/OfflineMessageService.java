package org.jivesoftware.of.common.message.offline;

import org.xmpp.packet.Message;

public interface OfflineMessageService {
	
	 void saveOfflineMsg(Message msg);

}
