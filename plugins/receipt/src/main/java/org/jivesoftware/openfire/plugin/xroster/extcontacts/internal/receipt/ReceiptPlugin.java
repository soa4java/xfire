package org.jivesoftware.openfire.plugin.xroster.extcontacts.internal.receipt;

import java.io.File;
import java.util.Map;

import org.jivesoftware.of.common.service.RestService;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.OfflineMessageStrategy;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.internal.receipt.handler.IQVersionHandler;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.internal.receipt.intercept.IncomingProcessedMessageListener;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.internal.receipt.intercept.IncomingUnProcessedMessageListener;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.internal.receipt.intercept.OfflineMessageListenerForReceipt;
import org.jivesoftware.openfire.plugin.xroster.extcontacts.internal.receipt.listener.ReceiptPresenceEventListener;
import org.jivesoftware.openfire.user.PresenceEventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	PacketInterceptor incomingProcessedMessageListener = new IncomingProcessedMessageListener();
	PacketInterceptor incomingUnProcessedMessageListener = new IncomingUnProcessedMessageListener(); 
	
	ReceiptPresenceEventListener receiptPresenceEventListener =new ReceiptPresenceEventListener();
	
	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		OfflineMessageStrategy.addListener(receiptOfflineMessageListener);
		
		PresenceEventDispatcher.addListener(receiptPresenceEventListener);
		
		InterceptorManager.getInstance().addInterceptor(incomingProcessedMsgListener);
		InterceptorManager.getInstance().addInterceptor(incomingUnProcessedMessageListener);

		iqRouter.addHandler(iqVersionHandler);
		
		MessageReSender.enable();

		System.out.println(ReceiptPlugin.class.getSimpleName() + " ok...");

		if (LOG.isInfoEnabled()) {
			LOG.info(ReceiptPlugin.class.getSimpleName() + " ok...");
		}
	}

	@Override
	public void destroyPlugin() {
		OfflineMessageStrategy.removeListener(receiptOfflineMessageListener);
		
		PresenceEventDispatcher.removeListener(receiptPresenceEventListener);
		
		InterceptorManager.getInstance().removeInterceptor(incomingProcessedMsgListener);
		InterceptorManager.getInstance().removeInterceptor(incomingUnProcessedMessageListener);
		
		iqRouter.removeHandler(iqVersionHandler);
		
		MessageReSender.disable();

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
