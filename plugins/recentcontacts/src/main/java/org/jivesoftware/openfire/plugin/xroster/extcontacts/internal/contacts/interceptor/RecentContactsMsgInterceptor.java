package org.jivesoftware.openfire.plugin.xroster.extcontacts.internal.contacts.interceptor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.yanrc.web.xweb.contacts.biz.service.RecentContactsApi;

import org.jivesoftware.of.common.spring.SpringContextHolder;
import org.jivesoftware.of.common.thread.XExecutor;
import org.jivesoftware.of.common.utils.ImPotocals;
import org.jivesoftware.of.common.utils.JidUtil;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

/**
 * 拦截所有消息，记录最近联系人
 */
public class RecentContactsMsgInterceptor implements PacketInterceptor {

	private final static Logger LOG = LoggerFactory.getLogger(RecentContactsMsgInterceptor.class);

	private final static String RECENT_CONTACT_KEY = "recent.contact.enable";
	private final static boolean RECENT_CONTACT_ENABLE_VAL = true;

	public static boolean recentContactEnable = JiveGlobals.getBooleanProperty(RECENT_CONTACT_KEY,
			RECENT_CONTACT_ENABLE_VAL);

	static {
		PropertyEventDispatcher.addListener(new RecentContactGroupEnablePropertyListener());
	}

	RecentContactsApi recentContactsApi;

	public RecentContactsMsgInterceptor() {
		recentContactsApi = SpringContextHolder.getBean("recentContactsApi", RecentContactsApi.class);
	}

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {
		try {
			if (recentContactEnable && incoming && !processed && (packet instanceof Message)) {

				final Message message = (Message) packet;
				XExecutor.globalExecutor.submit(new Runnable() {
					@Override
					public void run() {
						saveContactsAndMesageDigest(message);
					}
				});
			}
		} catch (Throwable t) {
			LOG.error("RecentContactsMsgInterceptor error !", t);
		}
	}

	private void saveContactsAndMesageDigest(final Message message) {
		try {

			if (ImPotocals.isBroadcastMsg(message)) {
				return;
			}

			if (Message.Type.chat == message.getType() && !JidUtil.nodeEqual(message)) {

				Set<String> partnerIds = new HashSet<String>(0);
				partnerIds.add(message.getTo().getNode());
				String fromPid = message.getFrom().getNode();
				recentContactsApi.saveContactsAndMesageDigest(false, fromPid, partnerIds, message.toXML());

				if (LOG.isInfoEnabled()) {
					LOG.info("cacheRecentContacts->chat id:{},msg:{}", message.getID(), message.toXML());
				}
			}
		} catch (Throwable t) {
			LOG.error("cacheRecentContacts error! msg:{}", message.toXML(), t);
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

}
