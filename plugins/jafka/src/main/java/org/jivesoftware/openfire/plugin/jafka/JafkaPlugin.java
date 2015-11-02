package org.jivesoftware.openfire.plugin.jafka;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.of.common.plugin.PluginAdaptor;
import org.jivesoftware.of.common.service.RestService;
import org.jivesoftware.openfire.OfflineMessageListener;
import org.jivesoftware.openfire.OfflineMessageStrategy;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
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

public class JafkaPlugin extends PluginAdaptor implements Plugin, OfflineMessageListener {
	private static Logger logger = LoggerFactory.getLogger(JafkaPlugin.class);
	private static PacketRouter packetRouter = XMPPServer.getInstance().getPacketRouter();
	private static ConcurrentLinkedQueue<Packet> packetQueue = new ConcurrentLinkedQueue<Packet>();

	private static String topic = "0.0.0.0_msgQueue";

	private Thread thread;

	private Producer<String, String> producer;
	private ConsumerConnector connector;
	private boolean flag = true;

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {

		initJafkaProductor();
		startMsgSendThread();

		OfflineMessageStrategy.addListener(this);

		new Thread(new Runnable() {

			@Override
			public void run() {
				Properties props = new Properties();
				props.put("zk.connect", "192.168.150.83:2181");
				props.put("groupid", "test_group");
				//
				ConsumerConfig consumerConfig = new ConsumerConfig(props);
				ConsumerConnector connector = Consumer.create(consumerConfig);
				//
				Map<String, List<MessageStream<String>>> topicMessageStreams = connector.createMessageStreams(
						ImmutableMap.of("0.0.0.0_msgQueue", 2), new StringDecoder());
				List<MessageStream<String>> streams = topicMessageStreams.get("0.0.0.0_msgQueue");
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

		/*initJafkaConsumer();
		

		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Properties props = new Properties();
				props.put("zk.connect", "192.168.150.83:2181");
				props.put("groupid", "test_group");
				//
				ConsumerConfig consumerConfig = new ConsumerConfig(props);
				ConsumerConnector connector = Consumer.create(consumerConfig);
				Map<String, List<MessageStream<String>>> topicMessageStreams = connector.createMessageStreams(
						ImmutableMap.of(topic, 2), new StringDecoder());
				List<MessageStream<String>> streams = topicMessageStreams.get(topic);
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

								System.out.println("受到消息： => " + message);

								System.out.println("收到消息:" + count.incrementAndGet() + "  => " + message);
							}
						}
					});
				}
				//
				try {
					executor.awaitTermination(3650, TimeUnit.DAYS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

		});

		thread.start();*/

		System.out.println("jafka ok...");
	}

	@Override
	public void destroyPlugin() {
		destroyJafkaProductor();
		destroyJafkaConsumer();

		flag = false;

		OfflineMessageStrategy.removeListener(this);
	}

	public void initJafkaProductor() {
		Properties props = new Properties();
		props.put("zk.connect", "192.168.150.83:2181");
		props.put("serializer.class", StringEncoder.class.getName());
		//
		ProducerConfig config = new ProducerConfig(props);
		producer = new Producer<String, String>(config);
		//
	}

	public void initJafkaConsumer() {
		Properties props = new Properties();
		props.put("zk.connect", "192.168.150.83:2181");
		props.put("groupid", "test_group" + System.currentTimeMillis());
		//
		ConsumerConfig consumerConfig = new ConsumerConfig(props);
		connector = Consumer.create(consumerConfig);
	}

	public void destroyJafkaConsumer() {
		if (connector != null) {
			try {
				connector.close();
			} catch (IOException e) {
				logger.error("destroyJafkaConsumer error!", e);
			}
		}
	}

	public void destroyJafkaProductor() {
		if (producer != null) {
			producer.close();
		}
	}

	private void receiveMsgs() {
		final PacketRouter packetRouter = XMPPServer.getInstance().getPacketRouter();
		final ExecutorService executor = Executors.newFixedThreadPool(2);
		while (flag) {

			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			Map<String, List<MessageStream<String>>> topicMessageStreams = connector.createMessageStreams(
					ImmutableMap.of(topic, 2), new StringDecoder());
			List<MessageStream<String>> streams = topicMessageStreams.get(topic);
			//
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

							System.out.println("受到消息： => " + message);
						}
					}
				});
			}
		}
	}

	private void saveOfflineMsg(Message msg) {

	}

	private void startMsgSendThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (flag) {

					try {

						long begin = System.currentTimeMillis();

						if (CollectionUtils.isEmpty(packetQueue)) {
							continue;
						}

						StringProducerData data = new StringProducerData(topic);

						Iterator<Packet> itr = packetQueue.iterator();
						Packet packet = null;
						while (null != (packet = packetQueue.poll())) {
							if (packet instanceof Message) {
								Message msg = (Message) packet;
								if (msg.getElement().attribute("mc") != null ) {
									saveOfflineMsg(msg);
									continue;
								}else{
									msg.getElement().addAttribute("mc","1");
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
						long cost = System.currentTimeMillis() - start;
						System.out.println("send message cost: " + cost + " ms:");
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

	public void messageDeleted(Message message) {
		enQueue(message);
	}

	@Override
	public void messageBounced(Message message) {
		enQueue(message);
	}

	@Override
	public void messageStored(Message message) {
		enQueue(message);
	}

	private void enQueue(Packet packet) {
		packetQueue.add(packet);
	}

}
