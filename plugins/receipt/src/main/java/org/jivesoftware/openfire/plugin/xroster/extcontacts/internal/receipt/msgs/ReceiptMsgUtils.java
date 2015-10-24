package org.jivesoftware.openfire.plugin.xroster.extcontacts.internal.receipt.msgs;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.openfire.XMPPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketExtension;

/**
 * 回执消息工厂
 *
 * @author yanricheng@163.com
 */
public abstract class ReceiptMsgUtils {
    public static final String MSG_ID = "id";
    public static final String RECEIPT_MSG_NAME = "received";
    public static final String SERVER_RECEIVED_NAMESPACE = "http://www.servyou.com.cn/protocol/server/received";
    public static final String CLIENT_RECEIVED_NAMESPACE = "http://www.servyou.com.cn/protocol/client/received";
    private static final Logger LOG = LoggerFactory.getLogger(ReceiptMsgUtils.class);
    private static final String GROUP_CHAT_DOMAIN = "groupchat."+XMPPServer.getInstance().getServerInfo().getXMPPDomain();

    public static boolean isSendToGroup(Message msg) {
        JID toJID = msg.getTo();
        if (toJID != null && GROUP_CHAT_DOMAIN.equals(toJID.getDomain())) {
            return true;
        }
        return false;
    }

    public static boolean isNeedReceipt(Message msg) {
        if ((msg.getType() == Message.Type.chat || msg.getType() == Message.Type.groupchat) && StringUtils.isNotBlank(msg.getID())
                /* null != msg.getElement().element("body") && null == msg.getElement().element(ServyouContants.RECEIPTED)*/) {
            return true;
        }

        return false;
    }

    public static boolean isOfflineMsg(Message msg) {
        return (null != msg.getChildElement("delay", "urn:xmpp:delay") && (null != msg.getChildElement("x", "jabber:x:delay")));
    }

    public static String getMsgIdFromClientReceiptMsg(Message msg) {
        String msgId = null;

        if (!(msg.getType() == Message.Type.normal)) {
            return msgId;
        }

        PacketExtension pe = msg.getExtension(ReceiptMsgUtils.RECEIPT_MSG_NAME, ReceiptMsgUtils.CLIENT_RECEIVED_NAMESPACE);
        if (pe != null) {
            msgId = pe.getElement().attributeValue("id");
            if (StringUtils.isBlank(msgId)) {
                LOG.warn("client receipt no msgId,客户端回执报没有消息id");
            }
        }
        return msgId;
    }

    public static Message createMsgReceipt(Message msg) {
        Message receipt = new Message();
        receipt.setFrom(msg.getTo());
        receipt.setTo(msg.getFrom());
        receipt.setID(msg.getID());
        receipt.addExtension(new PacketExtension(RECEIPT_MSG_NAME, SERVER_RECEIVED_NAMESPACE));
        receipt.getChildElement(RECEIPT_MSG_NAME, SERVER_RECEIVED_NAMESPACE).addAttribute(MSG_ID, msg.getID());
        return receipt;
    }
}