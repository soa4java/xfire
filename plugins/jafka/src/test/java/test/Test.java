package test;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

public class Test {

	public static void main(String[] args) {
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				Properties props = new Properties();
//				props.put("zk.connect", "192.168.150.83:2181");
//				props.put("serializer.class", StringEncoder.class.getName());
//				//
//				ProducerConfig config = new ProducerConfig(props);
//				Producer<String, String> producer = new Producer<String, String>(config);
//				//
//				StringProducerData data = new StringProducerData("0.0.0.0_msgQueue");
//
//				//
//				try {
//					for (int j = 0; j < 100; j++) {
//						
//						long start = System.currentTimeMillis();
//						for (int i = 0; i < 10; i++) {
//							data.add("Hello world #" + i);
//						}
//						
//						producer.send(data);
//						
//						long cost = System.currentTimeMillis() - start;
//						System.out.println("send 10 message cost: " + cost + " ms");
//
//						try {
//							Thread.sleep(5000);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//
//					}
//				} finally {
//					producer.close();
//				}
//
//			}
//		}).start();

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
							for (String message : stream) {
								System.out.println("收到消息:" + count.incrementAndGet() + "  => " + message);
							}
						}
					});
				}
				//
				try {
					executor.awaitTermination(1, TimeUnit.HOURS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

		}).start();
	}
}
