package org.jivesoftware.of.common.message;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

/**
 * 
 * @author yanricheng@163.com
 *
 */
public class MessageWatcher implements PacketInterceptor {

	volatile static MessageWatcher instance = new MessageWatcher();
	
	static{
		InterceptorManager.getInstance().addInterceptor(instance);
	}

	private MessageWatcher() {
	}

	public static MessageWatcher getInstance() {
		if (instance == null) {
			synchronized (MessageWatcher.class) {
				if (instance == null) {
					instance = new MessageWatcher();
				}
			}
		}
		return instance;
	}

	private static List<MessageListener> messageListenes = new ArrayList<MessageListener>();
	private static List<PresenceListener> presenceListeners = new ArrayList<PresenceListener>();

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {

		if (packet instanceof Message) {
			Message message = (Message) packet;
			for (MessageListener listener : messageListenes) {
				listener.process(message, session, incoming, processed);
			}
		} else if (packet instanceof Presence) {
			Presence presence = (Presence) packet;
			for (PresenceListener listener : presenceListeners) {
				listener.process(presence, session, incoming, processed);
			}
		}
	}

	public static boolean addPresenceListener(PresenceListener listener) {
		if (listener == null) {
			return false;
		}
		return presenceListeners.add(listener);
	}

	public static boolean removePresenceListener(PresenceListener listener) {
		if (listener == null) {
			return false;
		}
		return presenceListeners.remove(listener);
	}

	public static boolean addMessageListener(MessageListener listener) {
		if (listener == null) {
			return false;
		}
		return messageListenes.add(listener);
	}

	public static boolean removeMessageListener(MessageListener listener) {
		if (listener == null) {
			return false;
		}
		return messageListenes.remove(listener);
	}

}
