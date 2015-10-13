package com.servyou.openfire.plugin.receipt.listener;

import org.jivesoftware.openfire.OfflineMessageListener;
import org.xmpp.packet.Message;

import com.servyou.openfire.plugin.receipt.msgs.MessageClearer;

public class OfflineMessageListenerForReceipt implements OfflineMessageListener {

	//	static final String MSG_RESENT_SEND_FAIL_ENABLE = "message.receipt.send.fail.enable";
	//	static final boolean ENABLE_TRUE = true;
	//	static boolean enable = true;

	//	static {
	//		enable = JiveGlobals.getBooleanProperty(MSG_RESENT_SEND_FAIL_ENABLE, ENABLE_TRUE);
	//		PropertyEventDispatcher.addListener(new PropertyListener());
	//	}

	public OfflineMessageListenerForReceipt() {
	}

	@Override
	public void messageBounced(Message message) {
		MessageClearer.remove(message);
	}

	@Override
	public void messageStored(Message message) {
		MessageClearer.remove(message);
	}

	//	private static class PropertyListener implements PropertyEventListener {
	//		public void propertySet(String property, Map<String, Object> params) {
	//			if (MSG_RESENT_SEND_FAIL_ENABLE.equals(property)) {
	//				String value = (String) params.get("value");
	//				if (value != null) {
	//					enable = Boolean.valueOf(value);
	//				}
	//			}
	//		}
	//
	//		public void propertyDeleted(String property, Map<String, Object> params) {
	//			if (MSG_RESENT_SEND_FAIL_ENABLE.equals(property)) {
	//				enable = ENABLE_TRUE;
	//			}
	//		}
	//
	//		public void xmlPropertySet(String property, Map<String, Object> params) {
	//			// Do nothing
	//		}
	//
	//		public void xmlPropertyDeleted(String property, Map<String, Object> params) {
	//			// Do nothing
	//		}
	//	}

}
