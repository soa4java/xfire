package org.jivesoftware.openfire.plugin.jafka.service;

import org.xmpp.packet.Message;

public class ClusterOfflineMessageServiceImpl implements OfflineMessageService {
	
	private static OfflineMessageService instance;

	public static OfflineMessageService getInstance() {
		if (instance == null) {
			synchronized (ClusterOfflineMessageServiceImpl.class) {
				if (instance == null) {
					instance = new ClusterOfflineMessageServiceImpl();
				}
			}
		}

		return instance;
	}
	
	private ClusterOfflineMessageServiceImpl(){
		
	}

	@Override
	public void saveOfflineMsg(Message msg) {
		
	}

}
