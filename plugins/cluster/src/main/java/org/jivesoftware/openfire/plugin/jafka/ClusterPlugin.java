package org.jivesoftware.openfire.plugin.jafka;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.ext.listener.OnlineMessageListener;
import org.jivesoftware.of.common.message.PacketQueue;
import org.jivesoftware.of.common.node.ImNode;
import org.jivesoftware.of.common.node.ImNodes;
import org.jivesoftware.of.common.node.cache.NodeCache;
import org.jivesoftware.of.common.node.cache.impl.redis.RedisNodeCacheImpl;
import org.jivesoftware.of.common.plugin.PluginAdaptor;
import org.jivesoftware.of.common.service.RestService;
import org.jivesoftware.openfire.OfflineMessageStrategy;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.plugin.jafka.interceptor.MessageTimestampPacketInterceptor;
import org.jivesoftware.openfire.plugin.jafka.listener.ClusterOfflineMsgListener;
import org.jivesoftware.openfire.plugin.jafka.listener.ClusterOnlineMessageListener;
import org.jivesoftware.openfire.plugin.jafka.listener.presence.ClusterPresenceEventListener;
import org.jivesoftware.openfire.plugin.jafka.service.ClusterOfflineMessageServiceImpl;
import org.jivesoftware.openfire.plugin.jafka.service.OfflineMessageService;
import org.jivesoftware.openfire.plugin.jafka.zk.ZkUtils;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.spi.RoutingTableImpl;
import org.jivesoftware.openfire.user.PresenceEventDispatcher;
import org.jivesoftware.openfire.user.PresenceEventListener;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import com.sohu.jafka.consumer.Consumer;
import com.sohu.jafka.consumer.ConsumerConfig;
import com.sohu.jafka.consumer.ConsumerConnector;
import com.sohu.jafka.consumer.MessageStream;
import com.sohu.jafka.producer.Producer;
import com.sohu.jafka.producer.ProducerConfig;
import com.sohu.jafka.producer.StringProducerData;
import com.sohu.jafka.producer.serializer.StringDecoder;
import com.sohu.jafka.producer.serializer.StringEncoder;
import com.sohu.jafka.utils.ImmutableMap;

public class ClusterPlugin extends PluginAdaptor implements Plugin {
	private static Logger logger = LoggerFactory.getLogger(ClusterPlugin.class);
	private static PacketRouter packetRouter = XMPPServer.getInstance().getPacketRouter();
	private ClusterOfflineMsgListener offlineMsgListener = new ClusterOfflineMsgListener();
	private PresenceEventListener presenceEventListener = new ClusterPresenceEventListener();
	private PacketInterceptor messageInterceptor = new MessageTimestampPacketInterceptor();
	private  OnlineMessageListener onlineMessageListener = new ClusterOnlineMessageListener();

	public static String rootPath = "/imserver/nodes";
	public static String nodeIp = ImNodes.nodeIp;
	public static String nodeName = ImNodes.nodeName;

	public static String zkConnect;

	static {
		zkConnect = JiveGlobals.getXMLProperty("imserver.zk.connect");
	}

	private static String receiveTopic = nodeName + "_msgs";

	private Producer<String, String> producer;
	private ConsumerConnector connector;
	private boolean threadEnable = true;

	private NodeCache nodeCache;
	private OfflineMessageService offlineMessageService;

	public ClusterPlugin() {
		nodeCache = RedisNodeCacheImpl.getInstance();
		offlineMessageService = ClusterOfflineMessageServiceImpl.getInstance();
	}

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {

		if (StringUtils.isBlank(zkConnect) || StringUtils.isBlank(nodeName) || StringUtils.isBlank(nodeIp)) {
			String err = "没有配置集群信息..";
			System.err.println(err);
			throw new RuntimeException(err);
		}

		ZkUtils zkUtils = new ZkUtils(zkConnect);
		ImNode imNode = new ImNode(nodeName, nodeIp, System.currentTimeMillis());

		zkUtils.subscribeImNodeDataChanges(rootPath);
		zkUtils.registerImNode(rootPath, imNode);

		imNode = new ImNode(nodeName, nodeIp, System.currentTimeMillis());

		//		nodeCache.put(imNode.getName(), imNode);

		new Thread(new ImNodeHeartbeatRunnable(nodeCache), "#ImNodeHeartbeatThread" + nodeName).start();

		initJafkaProductor();

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (threadEnable) {
					try {

						Thread.currentThread().sleep(1000);

						long begin = System.currentTimeMillis();
						ConcurrentHashMap<String, ConcurrentLinkedQueue<Packet>> packetQueueMap = PacketQueue
								.getInstance().getPacketQueueMap();
						if (MapUtils.isEmpty(packetQueueMap)) {
							continue;
						}

						for (Map.Entry<String, ConcurrentLinkedQueue<Packet>> entry : packetQueueMap.entrySet()) {
							String topic = entry.getKey() + "_msgs";
							StringProducerData data = new StringProducerData(topic);
							ConcurrentLinkedQueue<Packet> queue = entry.getValue();

							if (CollectionUtils.isEmpty(queue)) {
								continue;
							}

							Packet packet = null;
							while (null != (packet = queue.poll())) {
								if (packet instanceof Message) {
									Message msg = (Message) packet;
									if (msg.getElement().attribute("mc") != null) {
										offlineMessageService.saveOfflineMsg(msg);
										continue;
									}
									data.add(packet.toXML());
								} else if (packet instanceof Presence) {
									data.add(packet.toXML());
								}

								packet.getElement().addAttribute("mc", "0");

								long end = System.currentTimeMillis();
								long interval = end - begin;
								if (data.getData().size() >= 100 || interval > 2000) {
									long start = System.currentTimeMillis();
									producer.send(data);
									long cost = System.currentTimeMillis() - start;
									System.out.println("send message cost: " + cost + " ms:,topic:" + topic + ",count:"
											+ data.getData().size());
									data.getData().clear();
									continue;
								}
							}

							long start = System.currentTimeMillis();
							producer.send(data);
							long cost = System.currentTimeMillis() - start;
							System.out.println("send message cost: " + cost + " ms:,topic:" + topic + ",count:"
									+ data.getData().size());
							data.getData().clear();
						}

					} catch (Exception e) {
						logger.error("send to jakfa error!", e);
						continue;
					}
				}
			}
		}, "#msgSendThread-" + nodeName).start();

		initJafkaConsumer();

		new Thread(new Runnable() {

			@Override
			public void run() {
				Map<String, List<MessageStream<String>>> topicMessageStreams = connector.createMessageStreams(
						ImmutableMap.of(receiveTopic, 2), new StringDecoder());
				List<MessageStream<String>> streams = topicMessageStreams.get(receiveTopic);
				//
				ExecutorService executor = Executors.newFixedThreadPool(2);
				final AtomicInteger count = new AtomicInteger();
				for (final MessageStream<String> stream : streams) {
					executor.submit(new Runnable() {

						public void run() {
							Packet packet = null;

							for (String message : stream) {
								Element ele = null;
								try {
									ele = DocumentHelper.parseText(message).getDocument().getRootElement();
								} catch (DocumentException e) {
									logger.error("parseText error! {}", message, e);
									continue;
								}

								if (packet != null) {
									packet.getElement().addAttribute("mc", "1");
								}

								if (message.startsWith("<message")) {
									packet = new Message(ele);
								} else if (message.startsWith("<presence")) {
									packet = new Presence(ele);
									JID fullJid = packet.getTo();
									ClientSession clientSession = SessionManager.getInstance().getSession(fullJid);
									if (clientSession != null) {
										clientSession.deliverRawText(packet.toXML());
									}

									continue;

								} else {
									logger.warn("unexpected package:{}", message);
								}

								packetRouter.route(packet);

								System.out.println("收到消息:" + count.incrementAndGet() + "  => " + message);
							}
						}
					});
				}
				//
				try {
					executor.awaitTermination(36500, TimeUnit.DAYS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

		}, "#msgReceiveThread-" + nodeName).start();

		OfflineMessageStrategy.addListener(offlineMsgListener);
		PresenceEventDispatcher.addListener(presenceEventListener);
		InterceptorManager.getInstance().addInterceptor(messageInterceptor);
		RoutingTableImpl.register(onlineMessageListener);

		System.out.println(this.getClass().getSimpleName() + " init succeed!");
	}

	@Override
	public void destroyPlugin() {

		threadEnable = false;
		nodeCache.delete(nodeName);
		destroyJafkaConsumer();
		destroyJafkaProductor();

		OfflineMessageStrategy.removeListener(offlineMsgListener);
		PresenceEventDispatcher.removeListener(presenceEventListener);
		InterceptorManager.getInstance().removeInterceptor(messageInterceptor);
		RoutingTableImpl.unregister(onlineMessageListener);

		System.out.println(this.getClass().getSimpleName() + " destroy succeed!");
	}

	public void initJafkaProductor() {
		Properties props = new Properties();
		props.put("zk.connect", zkConnect);
		props.put("serializer.class", StringEncoder.class.getName());
		ProducerConfig config = new ProducerConfig(props);
		producer = new Producer<String, String>(config);
	}

	public void initJafkaConsumer() {
		Properties props = new Properties();
		props.put("zk.connect", zkConnect);
		props.put("groupid", "imserver_group_" + nodeName);
		ConsumerConfig consumerConfig = new ConsumerConfig(props);
		connector = Consumer.create(consumerConfig);
	}

	public void destroyJafkaProductor() {
		if (producer != null) {
			producer.close();
		}
	}

	public void destroyJafkaConsumer() {
		if (connector != null) {
			connector.commitOffsets();
			try {
				connector.close();
			} catch (IOException e) {
				logger.error("destroyJafkaConsumer error!", e);
			}
		}
	}

	@Override
	public boolean register(String serviceName, RestService service) {
		return false;
	}

	@Override
	public boolean unregister() {
		return false;
	}

	class ImNodeHeartbeatRunnable implements Runnable {

		final Logger logger = LoggerFactory.getLogger(ImNodeHeartbeatRunnable.class);

		NodeCache nodeCache;

		public ImNodeHeartbeatRunnable(NodeCache nodeCache) {
			this.nodeCache = nodeCache;
		}

		@Override
		public void run() {
			while (threadEnable) {
				try {
					Thread.sleep(8000);
					String nodeName = ClusterPlugin.nodeName;
					String nodeIp = ClusterPlugin.nodeIp;
					ImNode imNode = new ImNode(nodeName, nodeIp, System.currentTimeMillis());
					nodeCache.put(nodeName, imNode);
				} catch (Exception e) {
					logger.error("ImNodeHeartbeatRunnable error!", e);
				}
			}

		}

	}

}
