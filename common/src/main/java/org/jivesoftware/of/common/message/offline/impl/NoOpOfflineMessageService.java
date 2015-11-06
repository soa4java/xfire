package org.jivesoftware.of.common.message.offline.impl;

import org.jivesoftware.of.common.message.offline.OfflineMessageService;
import org.xmpp.packet.Message;

public class NoOpOfflineMessageService implements OfflineMessageService {

	@Override
	public void saveOfflineMsg(Message msg) {

	}

}
