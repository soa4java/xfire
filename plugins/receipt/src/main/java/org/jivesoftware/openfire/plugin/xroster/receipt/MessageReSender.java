package org.jivesoftware.openfire.plugin.xroster.receipt;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.collections.CollectionUtils;
import org.jivesoftware.of.common.thread.XExecutor;
import org.jivesoftware.openfire.OfflineMessageStrategy;
import org.jivesoftware.openfire.RoutingTable;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.plugin.xroster.receipt.msgs.MessageQueueMap;
import org.jivesoftware.openfire.plugin.xroster.receipt.msgs.MessageQueueWaitingReceipt;
import org.jivesoftware.openfire.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketExtension;

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
						
						try {
							Thread.sleep(ReceiptsProps.scanInternalMills);
						} catch (InterruptedException e) {
							LOG.error("MessageReSender thread InterruptedException!", e);
						}
						
						Collection<ClientSession> localClientSessions = routingTable.getClientsRoutes(true);
						if (CollectionUtils.isNotEmpty(localClientSessions)) {
							for (ClientSession session : localClientSessions) {
								try {
									final MessageQueueWaitingReceipt queue = MessageQueueMap.get(session);
									long curTimeMillss = System.currentTimeMillis() ;
									if (CollectionUtils.isEmpty(queue)
											|| curTimeMillss - queue.getTimestamp() < ReceiptsProps.resentInternalMills) {
										continue;
									}

									final Message msg = queue.peek();
									
									if(msg == null){
										continue;
									}
									
									msg.getElement().addAttribute("resent", "true");
									
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
