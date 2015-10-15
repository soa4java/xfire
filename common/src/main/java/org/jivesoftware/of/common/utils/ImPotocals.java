package org.jivesoftware.of.common.utils;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.of.common.enums.ImPotocal;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketExtension;

/**
 * Created by yanrc on 2015/6/23.
 */
public abstract class ImPotocals {

	public static boolean shouldStoreMessage(Message message) {

		if (null == message.getType()) {
			return false;
		}

		if (JidUtil.nodeEqual(message)
				&& StringUtils.equalsIgnoreCase(message.getFrom().getResource(), message.getTo().getResource())) {
			return false;
		}

		if (Message.Type.normal == message.getType()) {
			if (isBroadcastMsg(message)) {
				return true;
			}

			if (null != message.getChildElement("notice", "http://www.servyou.com.cn/protocol/notice")) {
				return true;
			}
			if (null != message.getChildElement("cmd", "http://www.servyou.com.cn/protocol/cmd")) {
				return true;
			}
			return false;
		}

		// XEP-0334: Implement the <no-store/> hint to override offline storage
		if (isNoStoreMsg(message)) {
			return false;
		}

		if (isBizReadedMsg(message)) {
			return false;
		}

		if (isSynToSelfMsg(message)) {
			return false;
		}

		if (isAutoReply(message)) {
			return false;
		}

		return true;
	}

	public static boolean isNoStoreMsg(Message msg) {
		return msg.getChildElement("no-store", "urn:xmpp:hints") != null;
	}

	public static boolean isBroadcastMsg(Message msg) {
		return msg.getChildElement("broadcast", "http://www.servyou.com.cn/protocol/broadcast") != null;
	}

	public static boolean isAutoReply(Message msg) {
		return msg.getExtension("autoReply", "http://www.servyou.com.cn/protocol/autoReply") != null;
	}

	/**
	 * 是否是已读消息类型
	 *
	 * @param msg
	 * @return
	 */
	public static boolean isBizReadedMsg(Message msg) {
		PacketExtension msgRead1 = msg.getExtension(XConstants.READED, XConstants.URN_HINTS);//兼容老协议
		PacketExtension msgRead2 = msg.getExtension(ImPotocal.MsgReaded.extName(), ImPotocal.MsgReaded.extNamspace());//新协议
		return (msgRead1 != null) || (msgRead2 != null);
	}

	public static boolean isFileMsg(Message msg) {
		Element fileElement = msg.getChildElement(ImPotocal.FileMsg.extName(), ImPotocal.FileMsg.extNamspace());
		PacketExtension fileExtend = msg.getExtension(ImPotocal.FileMsg.extName(), ImPotocal.FileMsg.extNamspace());//新协议
		return (fileElement != null) || (fileExtend != null);
	}

	public static boolean isSynToSelfMsg(Message msg) {
		Element synToSelfEle = msg.getChildElement(XConstants.NO_SYN_TO_SELF, XConstants.URN_HINTS);//兼容老协议
		PacketExtension synToSelfExt = msg.getExtension(ImPotocal.SynToSelf.extName(),
				ImPotocal.SynToSelf.extNamspace());//新协议
		return (synToSelfEle != null) || (synToSelfExt != null);
	}

}
