package org.jivesoftware.of.common.message;

import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Message;

/**
 * 在线消息监听器
 * @author yanricheng@163.com
 *
 */
public interface MessageListener {
	
	public void process(Message message, Session session, boolean incoming, boolean processed);

}
