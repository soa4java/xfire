package org.jivesoftware.openfire.plugin.jafka;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jivesoftware.of.common.plugin.PluginAdaptor;
import org.jivesoftware.of.common.service.RestService;
import org.jivesoftware.openfire.OfflineMessageListener;
import org.jivesoftware.openfire.OfflineMessageStrategy;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Message;

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
	private Logger logger = LoggerFactory.getLogger(JafkaPlugin.class);
	private Producer<String, String> producer;

	public void initJafka() {
		Properties props = new Properties();
		props.put("zk.connect", "192.168.150.83:2181");
		props.put("serializer.class", StringEncoder.class.getName());
		//
		ProducerConfig config = new ProducerConfig(props);
		producer = new Producer<String, String>(config);
		//
	}

	public void destroyJafka() {
		producer.close();
	}

	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		initJafka();
		OfflineMessageStrategy.addListener(this);
		System.out.println("jafka ok...");
	}

	public void destroyPlugin() {
		destroyJafka();
		OfflineMessageStrategy.removeListener(this);
	}

	private boolean sendToJafka(Message message) {
		try {
			StringProducerData data = new StringProducerData("0.0.0.0_msgQueue");

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

		Properties props = new Properties();
		props.put("zk.connect", "192.168.150.83:2181");
		props.put("groupid", "test_group");
		//
		ConsumerConfig consumerConfig = new ConsumerConfig(props);
		ConsumerConnector connector = Consumer.create(consumerConfig);
		//
		Map<String, List<MessageStream<String>>> topicMessageStreams = connector.createMessageStreams(
				ImmutableMap.of("demo", 2), new StringDecoder());
		List<MessageStream<String>> streams = topicMessageStreams.get("demo");
		//
		ExecutorService executor = Executors.newFixedThreadPool(2);
		final AtomicInteger count = new AtomicInteger();
		for (final MessageStream<String> stream : streams) {
			executor.submit(new Runnable() {

				public void run() {
					for (String message : stream) {
						System.out.println(count.incrementAndGet() + " => " + message);
					}
				}
			});
		}
		//
		executor.awaitTermination(1, TimeUnit.HOURS);
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
