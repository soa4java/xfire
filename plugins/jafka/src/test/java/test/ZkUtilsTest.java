package test;

import java.util.concurrent.CountDownLatch;

import org.jivesoftware.openfire.plugin.jafka.vo.ImNode;
import org.jivesoftware.openfire.plugin.jafka.zk.ZkUtils;

public class ZkUtilsTest {

	public static void main(String[] args) {

//		CountDownLatch countDownLatch = new CountDownLatch(1);

		String rootPath = "/imserver/nodes";
		
		ZkUtils zkUtils = new ZkUtils("192.168.150.83:2181");
		ImNode imNode1 = new ImNode("nod100", "192.168.70.100", System.currentTimeMillis());
		ImNode imNode2 = new ImNode("nod200", "192.168.70.200", System.currentTimeMillis());
		
		zkUtils.subscribeImNodeDataChanges(rootPath);
		zkUtils.registerImNode(rootPath, imNode1);
		zkUtils.registerImNode(rootPath, imNode2);
		

//		try {
//			countDownLatch.wait();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		while(true){
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
