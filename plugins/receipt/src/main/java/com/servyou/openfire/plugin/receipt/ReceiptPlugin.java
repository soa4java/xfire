package com.servyou.openfire.plugin.receipt;

import java.io.File;
import java.util.Map;

import org.jivesoftware.of.common.message.MessageWatcher;
import org.jivesoftware.of.common.service.RestService;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.OfflineMessageStrategy;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.servyou.openfire.plugin.receipt.handler.IQVersionHandler;
import com.servyou.openfire.plugin.receipt.listener.IncomingProcessedMessageListener;
import com.servyou.openfire.plugin.receipt.listener.IncomingUnProcessedMessageListener;
import com.servyou.openfire.plugin.receipt.listener.OfflineMessageListenerForReceipt;

/**
 * 消息回执插件
 * 
 * @author yanricheng@163.com
 * 
 */
public class ReceiptPlugin implements Plugin {
	static Logger LOG = LoggerFactory.getLogger(ReceiptPlugin.class);

	IQRouter iqRouter = XMPPServer.getInstance().getIQRouter();
	IQVersionHandler iqVersionHandler = new IQVersionHandler();

	InterceptorManager interceptorManager = InterceptorManager.getInstance();

	OfflineMessageListenerForReceipt receiptOfflineMessageListener = new OfflineMessageListenerForReceipt();

	IncomingProcessedMessageListener incomingProcessedMsgListener = new IncomingProcessedMessageListener();
	IncomingUnProcessedMessageListener incomingUnProcessedMsgListener = new IncomingUnProcessedMessageListener();

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		OfflineMessageStrategy.addListener(receiptOfflineMessageListener);
		
		MessageWatcher.addMessageListener(incomingProcessedMsgListener);
		MessageWatcher.addMessageListener(incomingUnProcessedMsgListener);

		iqRouter.addHandler(iqVersionHandler);

		System.out.println(ReceiptPlugin.class.getSimpleName() + " ok...");

		if (LOG.isInfoEnabled()) {
			LOG.info(ReceiptPlugin.class.getSimpleName() + " ok...");
		}
	}

	@Override
	public void destroyPlugin() {
		OfflineMessageStrategy.removeListener(receiptOfflineMessageListener);
		
		MessageWatcher.removeMessageListener(incomingProcessedMsgListener);
		MessageWatcher.removeMessageListener(incomingUnProcessedMsgListener);
		
		iqRouter.removeHandler(iqVersionHandler);

		System.out.println(ReceiptPlugin.class.getSimpleName() + " destroy...");

		if (LOG.isInfoEnabled()) {
			LOG.info(ReceiptPlugin.class.getSimpleName() + " destroy...");
		}
	}

	/**
	 * 获取所有向插件注册api
	 * @return
	 */
	public Map<String, RestService> getRestServiceMap() {
		return null;
	}

}
