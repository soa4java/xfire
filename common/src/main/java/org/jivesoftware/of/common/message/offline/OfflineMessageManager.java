package org.jivesoftware.of.common.message.offline;

import org.jivesoftware.of.common.message.offline.impl.ClusterOfflineMessageServiceImpl;


public class OfflineMessageManager {

	private static OfflineMessageManager instance;

	private OfflineMessageService offlineMessageService = new ClusterOfflineMessageServiceImpl();

	public static OfflineMessageManager getInstance() {
		if (instance == null) {
			synchronized (OfflineMessageManager.class) {
				if (instance == null) {
					instance = new OfflineMessageManager();
				}
			}
		}

		return instance;
	}

	private OfflineMessageManager() {
	}

	public OfflineMessageService getOfflineMessageService() {
		return offlineMessageService;
	}

	public void setOfflineMessageService(OfflineMessageService offlineMessageService) {
		this.offlineMessageService = offlineMessageService;
	}

}
