package org.jivesoftware.openfire.plugin.jafka.zk;

import java.util.List;

import net.yanrc.app.common.util.JsonUtils;

import org.apache.zookeeper.CreateMode;
import org.jivesoftware.openfire.plugin.jafka.vo.ImNode;

import com.github.zkclient.IZkChildListener;
import com.github.zkclient.IZkClient;
import com.github.zkclient.ZkClient;
import com.github.zkclient.exception.ZkNoNodeException;
import com.github.zkclient.exception.ZkNodeExistsException;
import com.google.common.collect.Lists;

public class ZkUtils {

	private IZkClient zkClient;
	
	private int  defaultSessionTimeoutMills=10000;
	private int  defaultConnectionTimeoutMills=10000;

	public ZkUtils(String zkConnect) {
		zkClient = new ZkClient(zkConnect,defaultSessionTimeoutMills,defaultConnectionTimeoutMills);
	}

	public void registerImNode(String path, ImNode imNode) {

		if (!zkClient.exists(path)) {
			zkClient.createPersistent(path, true);
		}

		String subPath = path + "/" + imNode.getName();

		if (zkClient.exists(subPath)) {
			String err = imNode.getName() + "->节点已经注册，请修改 openfire.xml 配置重新启动";
			System.out.println(err);
			System.exit(0);
		}

		zkClient.createEphemeral(subPath, JsonUtils.fromObject(imNode).getBytes());
	}

	public void subscribeImNodeDataChanges(final String path) {
		zkClient.subscribeChildChanges(path, new IZkChildListener() {

			boolean inited = false;

			List<String> localCurrentChildrens =Lists.newArrayList();

			@Override
			public void handleChildChange(String parentPath, List<String> remoterCurrentChildrens) throws Exception {
				System.out.println("parentPath:" + parentPath + "currentChildren:" + remoterCurrentChildrens);

				if (remoterCurrentChildrens == null) {
					remoterCurrentChildrens = Lists.newArrayList();
				}

				if (!inited) {
					localCurrentChildrens = remoterCurrentChildrens;
					inited = true;
				} else {
					List<String> localChildrens = Lists.newArrayList();
					localChildrens.addAll(remoterCurrentChildrens);

					if (remoterCurrentChildrens.size() > localCurrentChildrens.size()) {
						remoterCurrentChildrens.removeAll(localCurrentChildrens);
						System.out.println("新增节点:" + JsonUtils.fromObject(remoterCurrentChildrens));
					} else {
						localCurrentChildrens.removeAll(remoterCurrentChildrens);
						System.out.println("减少节点:" + JsonUtils.fromObject(localCurrentChildrens));
					}
					localCurrentChildrens = localChildrens;
				}

			}

		});
	}

	public void createEphemeral(String path, boolean createParents) {
		try {
			zkClient.create(path, null, CreateMode.EPHEMERAL);
		} catch (ZkNodeExistsException e) {
			if (!createParents) {
				throw e;
			}
		} catch (ZkNoNodeException e) {
			if (!createParents) {
				throw e;
			}
			String parentDir = path.substring(0, path.lastIndexOf('/'));
			createEphemeral(parentDir, createParents);
			createEphemeral(path, createParents);
		}
	}

}
