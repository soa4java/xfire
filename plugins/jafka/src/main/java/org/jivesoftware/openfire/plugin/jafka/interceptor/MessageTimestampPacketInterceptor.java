package org.jivesoftware.openfire.plugin.jafka.interceptor;

import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class MessageTimestampPacketInterceptor implements PacketInterceptor {

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {
		if (packet instanceof Message) {
			if (incoming && !processed) {
				Message message = (Message) packet;
				if (Message.Type.chat == message.getType() || Message.Type.groupchat == message.getType()) {
					Timetamps.addTimetamp(message);
				}
			}
		}

	}

}
