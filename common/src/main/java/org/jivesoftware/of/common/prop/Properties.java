package org.jivesoftware.of.common.prop;

import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;

import java.util.Map;

/**
 * Created by yanrc on 15-8-3.
 */
public class Properties {

    final static String RECENT_CONTACTS_NUM_KEY = "recent.contacts.num";
    final static Long RECENT_CONTACTS_NUM_VAL = 20L;
    final static String RECENT_CONTACT_KEY = "recent.contact.enable";
    final static boolean RECENT_CONTACT_ENABLE_VAL = true;
    final static String GROUPCHAT_MSG_SEND_THREAD_NUM_KEY = "groupchat.msg.send.thread.num";
    final static int GROUPCHAT_MSG_SEND_THREAD_NUM_VAL = 200;

    final static String RECENT_CONTACT_SCAN_INTERNAL_MINS_KEY = "recent.contact.scan.internal.mins.key";
    final static Long RECENT_CONTACT_SCAN_INTERNAL_MINS_VAL = 1L;

    public static long recentContactNum = JiveGlobals.getLongProperty(RECENT_CONTACTS_NUM_KEY, RECENT_CONTACTS_NUM_VAL);
    public static boolean recentContactEnable = JiveGlobals.getBooleanProperty(RECENT_CONTACT_KEY, RECENT_CONTACT_ENABLE_VAL);
    public static int groupchatMsgSendThreadNum = JiveGlobals.getIntProperty(GROUPCHAT_MSG_SEND_THREAD_NUM_KEY, GROUPCHAT_MSG_SEND_THREAD_NUM_VAL);
    public static Long recentContactScanInternalMins = JiveGlobals.getLongProperty(RECENT_CONTACT_SCAN_INTERNAL_MINS_KEY, RECENT_CONTACT_SCAN_INTERNAL_MINS_VAL);

    static {
        PropertyEventDispatcher.addListener(new RecentContactsNumPropertyListener());
        PropertyEventDispatcher.addListener(new RecentContactGroupEnablePropertyListener());
        PropertyEventDispatcher.addListener(new GroupchatMsgSendThreadNumPropertyListener());
        PropertyEventDispatcher.addListener(new RecentContactScanInternalPropertyListener());
    }

    private static class GroupchatMsgSendThreadNumPropertyListener implements PropertyEventListener {
        public void propertySet(String property, Map<String, Object> params) {
            if (GROUPCHAT_MSG_SEND_THREAD_NUM_KEY.equals(property)) {
                String value = (String) params.get("value");
                if (value != null) {
                    groupchatMsgSendThreadNum = Integer.parseInt(value);
                }
            }

        }

        public void propertyDeleted(String property, Map<String, Object> params) {
            if (GROUPCHAT_MSG_SEND_THREAD_NUM_KEY.equals(property)) {
                groupchatMsgSendThreadNum = GROUPCHAT_MSG_SEND_THREAD_NUM_VAL;
            }
        }

        public void xmlPropertySet(String property, Map<String, Object> params) {
            // Do nothing
        }

        public void xmlPropertyDeleted(String property, Map<String, Object> params) {
            // Do nothing
        }
    }


    private static class RecentContactScanInternalPropertyListener implements PropertyEventListener {
        public void propertySet(String property, Map<String, Object> params) {
            if (RECENT_CONTACT_SCAN_INTERNAL_MINS_KEY.equals(property)) {
                String value = (String) params.get("value");
                if (value != null) {
                    recentContactScanInternalMins = Long.parseLong(value);
                }
            }

        }

        public void propertyDeleted(String property, Map<String, Object> params) {
            if (RECENT_CONTACT_SCAN_INTERNAL_MINS_KEY.equals(property)) {
                recentContactScanInternalMins = RECENT_CONTACT_SCAN_INTERNAL_MINS_VAL;
            }
        }

        public void xmlPropertySet(String property, Map<String, Object> params) {
            // Do nothing
        }

        public void xmlPropertyDeleted(String property, Map<String, Object> params) {
            // Do nothing
        }
    }


    private static class RecentContactGroupEnablePropertyListener implements PropertyEventListener {
        public void propertySet(String property, Map<String, Object> params) {
            if (RECENT_CONTACT_KEY.equals(property)) {
                String value = (String) params.get("value");
                if (value != null) {
                    recentContactEnable = Boolean.parseBoolean(value);
                }
            }

        }

        public void propertyDeleted(String property, Map<String, Object> params) {
            if (RECENT_CONTACT_KEY.equals(property)) {
                recentContactEnable = RECENT_CONTACT_ENABLE_VAL;
            }
        }

        public void xmlPropertySet(String property, Map<String, Object> params) {
            // Do nothing
        }

        public void xmlPropertyDeleted(String property, Map<String, Object> params) {
            // Do nothing
        }
    }

    private static class RecentContactsNumPropertyListener implements PropertyEventListener {
        public void propertySet(String property, Map<String, Object> params) {
            if (RECENT_CONTACTS_NUM_KEY.equals(property)) {
                String value = (String) params.get("value");
                if (value != null) {
                    recentContactNum = Long.parseLong(value);
                }
            }

        }

        public void propertyDeleted(String property, Map<String, Object> params) {
            if (RECENT_CONTACTS_NUM_KEY.equals(property)) {
                recentContactNum = RECENT_CONTACTS_NUM_VAL;
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
