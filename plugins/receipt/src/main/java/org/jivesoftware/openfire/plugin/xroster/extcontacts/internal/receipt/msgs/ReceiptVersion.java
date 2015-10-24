package org.jivesoftware.openfire.plugin.xroster.extcontacts.internal.receipt.msgs;

import java.util.Map;

import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;

public class ReceiptVersion {
	
	static final String MSG_RESENT_TARGET_VERSION = "message.receipt.target.version";
	static final String DEFAULT_TARGET_VERSION = "1.0.30";
	public static String pcVersion = "1.0.30";

    static final String MSG_RESENT_APP_TARGET_VERSION = "message.receipt.app.target.version";
    static final String DEFAULT_APP_TARGET_VERSION = "1.0.30";
    public static String appVersion = "1.0.30";

	static {
		pcVersion = JiveGlobals.getProperty(MSG_RESENT_TARGET_VERSION, DEFAULT_TARGET_VERSION);
        appVersion = JiveGlobals.getProperty(MSG_RESENT_APP_TARGET_VERSION, DEFAULT_APP_TARGET_VERSION);
		PropertyEventDispatcher.addListener(new PropertyListener());
	}
	
	private static class PropertyListener implements PropertyEventListener {
		public void propertySet(String property, Map<String, Object> params) {
			if (MSG_RESENT_TARGET_VERSION.equals(property)) {
				String value = (String) params.get("value");
				if (value != null) {
					pcVersion = value;
				}
			}else if (MSG_RESENT_APP_TARGET_VERSION.equals(property)) {
                String value = (String) params.get("value");
                if (value != null) {
                    appVersion = value;
                }
            }
		}

		public void propertyDeleted(String property, Map<String, Object> params) {
			if (MSG_RESENT_TARGET_VERSION.equals(property)) {
				pcVersion = DEFAULT_TARGET_VERSION;
			}else if (MSG_RESENT_APP_TARGET_VERSION.equals(property)) {
                appVersion = DEFAULT_APP_TARGET_VERSION;
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
