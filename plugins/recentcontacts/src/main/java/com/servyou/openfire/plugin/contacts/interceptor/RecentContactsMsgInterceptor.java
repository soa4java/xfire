package com.servyou.openfire.plugin.contacts.interceptor;

import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.servyou.constant.ServyouConstants;
import org.jivesoftware.openfire.servyou.constant.ServyouProperties;
import org.jivesoftware.openfire.servyou.spring.Hasher;
import org.jivesoftware.openfire.servyou.spring.RedisConstants;
import org.jivesoftware.openfire.servyou.spring.SpringContextHolder;
import org.jivesoftware.openfire.servyou.utils.ImPotocals;
import org.jivesoftware.openfire.servyou.utils.JidUtil;
import org.jivesoftware.openfire.servyou.utils.ServyouNamedThreadFactory;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import sitong.thinker.common.util.JsonUtils;
import sitong.thinker.of.biz.contact.service.RecentContactsService;
import sitong.thinker.of.biz.digest.service.MsgDigestService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 拦截所有消息，记录最近联系人
 */
public class RecentContactsMsgInterceptor implements PacketInterceptor {

	private final static Logger LOG = LoggerFactory.getLogger(RecentContactsMsgInterceptor.class);

	private static ExecutorService chatMsgExecutorService = Executors.newFixedThreadPool(JiveGlobals.getIntProperty(
			"chat.msg.send.thread.num", 100), new ServyouNamedThreadFactory("chatMsgSend", true));

	private RecentContactsService recentContactsService;

	private MsgDigestService msgDigestService;

	public RecentContactsMsgInterceptor() {
		recentContactsService = SpringContextHolder.getBean(RecentContactsService.class);
		msgDigestService = SpringContextHolder.getBean(MsgDigestService.class);
	}

	public static ExecutorService getChatMsgExecutorService() {
		return chatMsgExecutorService;
	}

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {
		try {
			if (ServyouProperties.recentContactEnable && incoming && !processed && (packet instanceof Message)) {

				final Message message = (Message) packet;
				chatMsgExecutorService.submit(new Runnable() {
					@Override
					public void run() {
						saveMsgDigest(message);
						saveRecentContacts(message);
					}
				});
			}
		} catch (Throwable t) {
			LOG.error("RecentContactsMsgInterceptor error !", t);
		}
	}

	private void saveMsgDigest(Message message) {

		String key = null;
		//存放最后一条消息
		Map<String, String> map = new HashMap<String, String>();

		try {

			if (ImPotocals.isBroadcastMsg(message)) {
				return;
			}

			if (Message.Type.chat == message.getType()) {//广播消息不存摘要
				String fromNode = message.getFrom().getNode();
				String toNode = message.getTo().getNode();
				key = Hasher.hashByHashCode(fromNode, toNode);
				map.put(ServyouConstants.KEY, key);
				map.put(ServyouConstants.MSG, message.toXML());

				msgDigestService.saveOrUpdate(key, JsonUtils.fromObject(map));

				if (LOG.isInfoEnabled()) {
					LOG.info("cacheLastMsgHash->chat id:{},msg:{}", message.getID(), message.toXML());
				}
			}else if (Message.Type.groupchat == message.getType()) {
				
			}
			
		} catch (Throwable t) {
			LOG.error("cacheLastMsgHash error! hash:{},key:{}; msg:{}", RedisConstants.HASH_DIGEST, key,
					message.toXML(), t);
		}

	}

	private void saveRecentContacts(final Message message) {
		try {
			if ((Message.Type.chat == message.getType() || ImPotocals.isBroadcastMsg(message))
					&& !JidUtil.nodeEqual(message)) {

				recentContactsService.saveChat(message.getFrom().getNode(), message.getTo().getNode());

				if (LOG.isInfoEnabled()) {
					LOG.info("cacheRecentContacts->chat id:{},msg:{}", message.getID(), message.toXML());
				}
			}
		} catch (Throwable t) {
			LOG.error("cacheRecentContacts error! msg:{}", message.toXML(), t);
		}

	}

}
