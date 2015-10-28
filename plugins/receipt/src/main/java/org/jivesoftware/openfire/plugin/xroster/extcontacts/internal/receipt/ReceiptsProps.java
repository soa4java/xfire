package org.jivesoftware.openfire.plugin.xroster.extcontacts.internal.receipt;

import java.util.Map;

import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;

public abstract class ReceiptsProps {

	private static final long VALUE_DEFAULT_INTERNAL_MILLS = 500L;
	private static final String KEY_MSG_RESENT_SCAN_INTERVAL_MILLS = "message.resent.scan.internal.mills";
	private static final String KEY_RECEIPT_ERR_OUT_ENABLE = "receipt.err.out.enable";
	private static final String KEY_MSG_ENQUEUE_LOG_ENABLE = "msg.enqueue.log.enable";
	private final static String KEY_MSG_RESENT_INTERVAL_SECENDS = "message.resent.internal.seconds";
	private final static String KEY_MSG_SENT_NUM_OF_TIMES = "message.sent.num.of.times";
	/** 默认间隔30s重发一次 */
	private final static long VALUE_DEFAULT_INTERNAL_SENONDS = 30L;
	/** 默认重发1次*/
	private final static int VALUE_DEFAULT_RESENT_NUM = 1;

	public static long resentInternalMills = 0;
	public static long sentNumOfTimes = 0;
	public static boolean errorOutEnable = false;
	public static long scanInternalMills = 0;
	public static boolean msgEnqueueLogEnable = false;

	static {
		msgEnqueueLogEnable = JiveGlobals.getBooleanProperty(KEY_MSG_ENQUEUE_LOG_ENABLE, false);
		resentInternalMills = JiveGlobals.getLongProperty(KEY_MSG_RESENT_INTERVAL_SECENDS,
				VALUE_DEFAULT_INTERNAL_SENONDS) * 1000;
		sentNumOfTimes = JiveGlobals.getLongProperty(KEY_MSG_SENT_NUM_OF_TIMES, VALUE_DEFAULT_RESENT_NUM);

		scanInternalMills = JiveGlobals.getLongProperty(KEY_MSG_RESENT_SCAN_INTERVAL_MILLS,
				VALUE_DEFAULT_INTERNAL_MILLS);
		errorOutEnable = JiveGlobals.getBooleanProperty(KEY_RECEIPT_ERR_OUT_ENABLE, false);

		PropertyEventDispatcher.addListener(new ReceiptLogPropertyListener());
	}

	private static class ReceiptLogPropertyListener implements PropertyEventListener {
		public void propertySet(String property, Map<String, Object> params) {
			if (KEY_MSG_ENQUEUE_LOG_ENABLE.equals(property)) {
				String value = (String) params.get("value");
				if (value != null) {
					msgEnqueueLogEnable = Boolean.parseBoolean(value);
				}
			} else if (KEY_MSG_RESENT_INTERVAL_SECENDS.equals(property)) {
				String value = (String) params.get("value");
				if (value != null) {
					resentInternalMills = Long.parseLong(value) * 1000;
				}
			} else if (KEY_MSG_SENT_NUM_OF_TIMES.equals(property)) {
				String value = (String) params.get("value");
				if (value != null) {
					sentNumOfTimes = Integer.parseInt(value);
				}
			} else if (KEY_MSG_RESENT_SCAN_INTERVAL_MILLS.equals(property)) {
				String value = (String) params.get("value");
				if (value != null) {
					scanInternalMills = Integer.parseInt(value);
				}
			} else if (KEY_RECEIPT_ERR_OUT_ENABLE.equals(property)) {
				String value = (String) params.get("value");
				if (value != null) {
					errorOutEnable = Boolean.parseBoolean(value);
				}
			}
		}

		public void propertyDeleted(String property, Map<String, Object> params) {
			if (KEY_MSG_ENQUEUE_LOG_ENABLE.equals(property)) {
				msgEnqueueLogEnable = false;
			} else if (KEY_MSG_RESENT_INTERVAL_SECENDS.equals(property)) {
				resentInternalMills = VALUE_DEFAULT_INTERNAL_SENONDS * 1000;
			} else if (KEY_MSG_SENT_NUM_OF_TIMES.equals(property)) {
				sentNumOfTimes = VALUE_DEFAULT_RESENT_NUM;
			} else if (KEY_MSG_RESENT_SCAN_INTERVAL_MILLS.equals(property)) {
				scanInternalMills = VALUE_DEFAULT_INTERNAL_MILLS;
			} else if (KEY_RECEIPT_ERR_OUT_ENABLE.equals(property)) {
				errorOutEnable = false;
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
