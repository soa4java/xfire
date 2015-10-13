package com.servyou.openfire.plugin.receipt;

import java.util.Map;

import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;

public abstract class ReceiptsProps {

	private static final String MSG_ENQUEUE_LOG_ENABLE = "msg.enqueue.log.enable";
	public static boolean msgEnqueueLogEnable = false;

	private final static String MSG_RESENT_INTERVAL_SECENDS = "message.resent.internal.seconds";
	private final static String MSG_SENT_NUM_OF_TIMES = "message.sent.num.of.times";
	/** 默认间隔3s重发一次 */
	private final static long DEFAULT_INTERNAL_SENONDS = 3L;
	/** 默认重发1次*/
	private final static int DEFAULT_RESENT_NUM = 2;

	public static long resentInternalMills = 0;
	public static long sentNumOfTimes = 0;

	static {
		msgEnqueueLogEnable = JiveGlobals.getBooleanProperty(MSG_ENQUEUE_LOG_ENABLE, false);
		PropertyEventDispatcher.addListener(new ReceiptLogPropertyListener());

		resentInternalMills = JiveGlobals.getLongProperty(MSG_RESENT_INTERVAL_SECENDS, DEFAULT_INTERNAL_SENONDS) * 1000;
		sentNumOfTimes = JiveGlobals.getLongProperty(MSG_SENT_NUM_OF_TIMES, DEFAULT_RESENT_NUM);
		PropertyEventDispatcher.addListener(new ReceiptInternalPropertyListener());
	}

	private static class ReceiptLogPropertyListener implements PropertyEventListener {
		public void propertySet(String property, Map<String, Object> params) {
			if (MSG_ENQUEUE_LOG_ENABLE.equals(property)) {
				String value = (String) params.get("value");
				if (value != null) {
					msgEnqueueLogEnable = Boolean.parseBoolean(value);
				}
			}
		}

		public void propertyDeleted(String property, Map<String, Object> params) {
			if (MSG_ENQUEUE_LOG_ENABLE.equals(property)) {
				msgEnqueueLogEnable = false;
			}
		}

		public void xmlPropertySet(String property, Map<String, Object> params) {
			// Do nothing
		}

		public void xmlPropertyDeleted(String property, Map<String, Object> params) {
			// Do nothing
		}
	}

	private static class ReceiptInternalPropertyListener implements PropertyEventListener {
		public void propertySet(String property, Map<String, Object> params) {
			if (MSG_RESENT_INTERVAL_SECENDS.equals(property)) {
				String value = (String) params.get("value");
				if (value != null) {
					resentInternalMills = Long.parseLong(value) * 1000;
				}
			} else if (MSG_SENT_NUM_OF_TIMES.equals(property)) {
				String value = (String) params.get("value");
				if (value != null) {
					sentNumOfTimes = Integer.parseInt(value);
				}
			}

		}

		public void propertyDeleted(String property, Map<String, Object> params) {
			if (MSG_RESENT_INTERVAL_SECENDS.equals(property)) {
				resentInternalMills = DEFAULT_INTERNAL_SENONDS * 1000;
			} else if (MSG_SENT_NUM_OF_TIMES.equals(property)) {
				sentNumOfTimes = DEFAULT_RESENT_NUM;
			}
		}

		public void xmlPropertySet(String property, Map<String, Object> params) {
			// Do nothing
		}

		public void xmlPropertyDeleted(String property, Map<String, Object> params) {
			// Do nothing
		}
	}

}
