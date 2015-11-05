package org.jivesoftware.openfire.plugin.xroster.receipt.intercept;

import org.jivesoftware.openfire.OfflineMessageListener;
import org.jivesoftware.openfire.plugin.xroster.receipt.ReceiptConstants;
import org.jivesoftware.openfire.plugin.xroster.receipt.msgs.MessageClearer;
import org.xmpp.packet.Message;

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

	@Override
	public void messageDeleted(Message message) {
		remove(message);
	}
}
