package com.servyou.openfire.plugin.receipt;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.collections.CollectionUtils;
import org.jivesoftware.of.common.thread.XExecutor;
import org.jivesoftware.openfire.OfflineMessageStrategy;
import org.jivesoftware.openfire.RoutingTable;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketExtension;

import com.servyou.openfire.plugin.receipt.msgs.MessageClearer;
import com.servyou.openfire.plugin.receipt.msgs.MessageQueueMap;
import com.servyou.openfire.plugin.receipt.msgs.MessageQueueWaitingReceipt;

public class MessageReSender {
	private static final Logger LOG = LoggerFactory.getLogger(MessageReSender.class);
	private static OfflineMessageStrategy messageStrategy = XMPPServer.getInstance().getOfflineMessageStrategy();
	private static RoutingTable routingTable = XMPPServer.getInstance().getRoutingTable();
	private static volatile boolean enabled;
	private static ThreadPoolExecutor executor;
	private static int nThread = 3;
	private static String prefix = "MessageReSender";

	public static void enable() {
		enabled = true;
		executor = XExecutor.newFixedThreadPool(nThread, prefix);

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					if (enabled) {
						Collection<ClientSession> localClientSessions = routingTable.getClientsRoutes(true);
						if (CollectionUtils.isNotEmpty(localClientSessions)) {
							for (ClientSession session : localClientSessions) {
								try {
									final MessageQueueWaitingReceipt queue = MessageQueueMap.get(session);

									if (CollectionUtils.isEmpty(queue)
											|| System.currentTimeMillis() - queue.getTimestamp() < ReceiptsProps.resentInternalMills) {
										continue;
									}

									final Message msg = queue.peek();
									final ClientSession clientSession = session;

									if (queue.getCount() < ReceiptsProps.sentNumOfTimes) {

										queue.countIncrease(1);

										executor.submit(new Runnable() {

											@Override
											public void run() {
												clientSession.deliverRawText(msg.toXML());
											}
										});
									} else {
										msg.addExtension(new PacketExtension("x", ReceiptConstants.resendToOffline));
										messageStrategy.storeOffline(msg);
									}
								} catch (Throwable t) {
									LOG.error("MessageReSender thread error!", t);
								}

								try {
									Thread.sleep(ReceiptsProps.scanInternalMills);
								} catch (InterruptedException e) {
									LOG.error("MessageReSender thread InterruptedException!", e);
								}
							}
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
