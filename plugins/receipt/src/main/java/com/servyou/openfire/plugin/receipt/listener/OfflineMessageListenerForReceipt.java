package com.servyou.openfire.plugin.receipt.listener;

import org.jivesoftware.openfire.OfflineMessageListener;
import org.xmpp.packet.Message;

import com.servyou.openfire.plugin.receipt.ReceiptConstants;
import com.servyou.openfire.plugin.receipt.msgs.MessageClearer;

public class OfflineMessageListenerForReceipt implements OfflineMessageListener {

	public OfflineMessageListenerForReceipt() {
	}

	@Override
	public void messageBounced(Message message) {
		remove(message);
	}

	@Override
	public void messageStored(Message message) {
		remove(message);
	}

	private void remove(Message message) {
		if (message.getExtension("x", ReceiptConstants.resendToOffline) != null) {
			message.deleteExtension("x", ReceiptConstants.resendToOffline);
			MessageClearer.removeByToBaredJID(message);
		} else {
			MessageClearer.removeByFromFullJID(message);
		}
	}
}
