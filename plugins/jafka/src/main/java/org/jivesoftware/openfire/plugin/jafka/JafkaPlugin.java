package org.jivesoftware.openfire.plugin.jafka;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.of.common.plugin.PluginAdaptor;
import org.jivesoftware.of.common.service.RestService;
import org.jivesoftware.openfire.OfflineMessageStrategy;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.plugin.jafka.cache.NodeCache;
import org.jivesoftware.openfire.plugin.jafka.cache.impl.redis.RedisNodeCacheImpl;
import org.jivesoftware.openfire.plugin.jafka.listener.JafakaOfflineMsgListener;
import org.jivesoftware.openfire.plugin.jafka.listener.JafkaPresenceEventListener;
import org.jivesoftware.openfire.plugin.jafka.service.OfflineMessageService;
import org.jivesoftware.openfire.plugin.jafka.service.OfflineMessageServiceImpl;
import org.jivesoftware.openfire.plugin.jafka.vo.ImNode;
import org.jivesoftware.openfire.user.PresenceEventDispatcher;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class JafkaPlugin extends PluginAdaptor implements Plugin {
	private static Logger logger = LoggerFactory.getLogger(JafkaPlugin.class);
	private static PacketRouter packetRouter = XMPPServer.getInstance().getPacketRouter();
	private final JafakaOfflineMsgListener offlineMsgListener = new JafakaOfflineMsgListener();
	JafkaPresenceEventListener presenceEventListener = new JafkaPresenceEventListener();

	public static String zkConnect;
	public static String nodeName;
	public static String nodeIp;

	static {
		zkConnect = JiveGlobals.getXMLProperty("imserver.zk.connect");
		nodeName = JiveGlobals.getXMLProperty("imserver.node.name");
		nodeIp = JiveGlobals.getXMLProperty("imserver.node.ip");
	}

	private Thread thread;

	private Producer<String, String> producer;
	private ConsumerConnector connector;
	private boolean threadEnable = true;

	private NodeCache nodeCache;
	private OfflineMessageService offlineMessageService;

	public JafkaPlugin() {
		nodeCache = RedisNodeCacheImpl.getInstance();
		offlineMessageService = OfflineMessageServiceImpl.getInstance();
	}

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		if (StringUtils.isBlank(zkConnect) || StringUtils.isBlank(nodeName) || StringUtils.isBlank(nodeIp)) {
			String err = "没有配置集群信息..";
			System.err.println(err);
			throw new RuntimeException(err);
		}

		ImNode imNode = nodeCache.get(nodeName);
		if (imNode != null) {
			String err = "存在重复的节点名字";
			System.err.println(err);
			throw new RuntimeException(err);
		}

		imNode = new ImNode(nodeName, nodeIp, System.currentTimeMillis());

		nodeCache.put(imNode.getName(), imNode);

		initJafkaProductor();
		startMsgSendThread();

		OfflineMessageStrategy.addListener(offlineMsgListener);
		PresenceEventDispatcher.addListener(presenceEventListener);
		
		new Thread(new ImNodeHeartbeatRunnable(nodeCache),"#ImNodeHeartbeatThread").start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				Properties props = new Properties();
				props.put("zk.connect", zkConnect);
				props.put("groupid", "imserver_group_" + nodeName);
				String topid = nodeName + "_msgs";
				//
				ConsumerConfig consumerConfig = new ConsumerConfig(props);
				ConsumerConnector connector = Consumer.create(consumerConfig);
				//
				Map<String, List<MessageStream<String>>> topicMessageStreams = connector.createMessageStreams(
						ImmutableMap.of(topid, 2), new StringDecoder());
				List<MessageStream<String>> streams = topicMessageStreams.get(topid);
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

								if (message.startsWith("<message")) {
									packet = new Message(ele);
								} else if (message.startsWith("<presence")) {
									packet = new Presence(ele);
								} else {
									logger.warn("unexpected package:{}", message);
								}

								if (packet != null) {
									packet.getElement().addAttribute("mc", "1");
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

		}).start();

		System.out.println("jafka ok...");
	}

	@Override
	public void destroyPlugin() {
		destroyJafkaProductor();
		threadEnable = false;

		OfflineMessageStrategy.removeListener(offlineMsgListener);
		PresenceEventDispatcher.removeListener(presenceEventListener);

		System.out.println(this.getClass().getSimpleName() + " destroy succeed!");
	}

	public void initJafkaProductor() {
		Properties props = new Properties();
		props.put("zk.connect", zkConnect);
		props.put("serializer.class", StringEncoder.class.getName());
		ProducerConfig config = new ProducerConfig(props);
		producer = new Producer<String, String>(config);
	}

	public void destroyJafkaProductor() {

		nodeCache.delete(nodeName);

		if (producer != null) {
			producer.close();
		}
	}

	private void startMsgSendThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (threadEnable) {
					try {
						long begin = System.currentTimeMillis();
						ConcurrentHashMap<String, ConcurrentLinkedQueue<Packet>> packetQueueMap = offlineMsgListener
								.getPacketQueueMap();
						if (MapUtils.isEmpty(packetQueueMap)) {
							continue;
						}

						for (Map.Entry<String, ConcurrentLinkedQueue<Packet>> entry : packetQueueMap.entrySet()) {
							String topic = entry.getKey()+ "_msgs";;
							StringProducerData data = new StringProducerData(topic);
							ConcurrentLinkedQueue<Packet> queue = entry.getValue();
							Packet packet = null;
							while (null != (packet = queue.poll())) {
								if (packet instanceof Message) {
									Message msg = (Message) packet;
									if (msg.getElement().attribute("mc") != null) {
										offlineMessageService.saveOfflineMsg(msg);
										continue;
									} else {
										msg.getElement().addAttribute("mc", "1");
									}

									long end = System.currentTimeMillis();
									long interval = end - begin;

									data.add(packet.toXML());

									if (data.getData().size() >= 100 || interval > 2000) {
										long start = System.currentTimeMillis();
										producer.send(data);
										long cost = System.currentTimeMillis() - start;
										System.out.println("send message cost: " + cost + " ms:");
										data.getData().clear();
										continue;
									}
								}
							}

							long start = System.currentTimeMillis();
							producer.send(data);
							data.getData().clear();
							long cost = System.currentTimeMillis() - start;
							System.out.println("send message cost: " + cost + " ms:");
						}

					} catch (Exception e) {
						logger.error("send to jakfa error!", e);
						continue;
					}
				}
			}
		}).start();

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
					String nodeName = JafkaPlugin.nodeName;
					String nodeIp = JafkaPlugin.nodeIp;
					ImNode imNode = new ImNode(nodeName, nodeIp, System.currentTimeMillis());
					nodeCache.put(nodeName, imNode);
				} catch (Exception e) {
					logger.error("ImNodeHeartbeatRunnable error!", e);
				}
			}

		}

	}

}
