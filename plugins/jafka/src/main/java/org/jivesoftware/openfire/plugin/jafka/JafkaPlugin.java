package org.jivesoftware.openfire.plugin.jafka;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.text.Document;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMPPPacketReader;
import org.jivesoftware.of.common.plugin.PluginAdaptor;
import org.jivesoftware.of.common.service.RestService;
import org.jivesoftware.openfire.OfflineMessageListener;
import org.jivesoftware.openfire.OfflineMessageStrategy;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.net.MXParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
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
	private static String CHARSET = "UTF-8";
	private static String topic = "0.0.0.0_msgQueue";
	private static XmlPullParserFactory factory = null;

	private static XMPPPacketReader reader = null;
	private Thread thread;

	private Producer<String, String> producer;
	private ConsumerConnector connector;
	private boolean flag = true;

	static {
		try {
			factory = XmlPullParserFactory.newInstance(MXParser.class.getName(), null);
			reader = new XMPPPacketReader();
			reader.setXPPFactory(factory);
		} catch (XmlPullParserException e) {
			logger.error("Error creating a parser factory", e);
		}
	}

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		initJafkaProductor();
		initJafkaConsumer();
		OfflineMessageStrategy.addListener(this);

		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				receiveMsgs();
			}
		});

		thread.start();

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
		props.put("groupid", "test_group"+System.currentTimeMillis());
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
			Map<String, List<MessageStream<String>>> topicMessageStreams = connector.createMessageStreams(
					ImmutableMap.of(topic, 2), new StringDecoder());
			List<MessageStream<String>> streams = topicMessageStreams.get(topic);
			//

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

							System.out.println(count.incrementAndGet() + " => " + message);
						}
					}
				});
			}
		}
	}

	private boolean sendToJafka(Message message) {
		try {

			if (message.getElement().attribute("mc") != null) {
				//存离线
				return true;
			}

			StringProducerData data = new StringProducerData(topic);

			//同时发多个
			data.add(message.toXML());

			long start = System.currentTimeMillis();

			/*for (int i = 0; i < 100; i++) {
			    producer.send(data);
			}*/

			producer.send(data);

			long cost = System.currentTimeMillis() - start;
			System.out.println("send 10000 message cost: " + cost + " ms");

			return true;

		} catch (Exception e) {
			logger.error("send to jakfa error!", e);
			return false;
		}
	}

	public static void main(String[] args) throws Exception {

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
		if (sendToJafka(message)) {
			System.out.println("send succeed!");
		}
	}

	@Override
	public void messageBounced(Message message) {
	}

	@Override
	public void messageStored(Message message) {

	}

}
