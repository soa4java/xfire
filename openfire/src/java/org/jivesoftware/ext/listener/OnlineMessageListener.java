package org.jivesoftware.ext.listener;

import org.xmpp.packet.Message;

public interface OnlineMessageListener {
	public void onSucceed(Message message);
}
