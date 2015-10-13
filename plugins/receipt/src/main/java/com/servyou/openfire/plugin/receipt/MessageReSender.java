package com.servyou.openfire.plugin.receipt;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

import org.jivesoftware.of.common.thread.XExecutor;
import org.jivesoftware.openfire.RoutingTable;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ClientSession;
import org.xmpp.packet.Message;

import com.servyou.openfire.plugin.receipt.msgs.MessageQueueMap;
import com.servyou.openfire.plugin.receipt.msgs.MessageQueueWaitingReceipt;

public class MessageReSender {

	static RoutingTable routingTable = XMPPServer.getInstance().getRoutingTable();
	static XMPPServer server = XMPPServer.getInstance();
	static volatile boolean enabled;
	static ThreadPoolExecutor executor;
	static int nThread = 3;
	static String prefix = "MessageReSender";

	public static void enable() {
		enabled = true;
		executor = XExecutor.newFixedThreadPool(nThread, prefix);

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					if (enabled) {
						Collection<ClientSession> localClientSessions = routingTable.getClientsRoutes(true);
						for (ClientSession session : localClientSessions) {
							final MessageQueueWaitingReceipt queue = MessageQueueMap.get(session);

							if (System.currentTimeMillis() - queue.getTimestamp() < ReceiptsProps.resentInternalMills) {
								continue;
							}

							executor.submit(new Runnable() {

								@Override
								public void run() {
									Message msg = queue.peek();
									server.getPacketRouter().route(msg);
								}
							});
						}
					} else {
						return;
					}
				}

			}
		}, "MessageReSenderThread").start();

	}

	public static void disable() {
		enabled = false;
		executor.shutdown();
		executor = null;
	}

}
