package org.jivesoftware.of.common.error;

import java.util.Map;

import net.yanrc.app.common.api.Head;
import net.yanrc.app.common.error.MessageXmlDomReader;
import net.yanrc.app.common.result.DefaultResult;
import net.yanrc.app.common.result.Result;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.of.common.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

public abstract class XmppErrorMessageUtils {
	protected static Logger LOG = LoggerFactory.getLogger(XmppErrorMessageUtils.class);

	private static final Map<String, String> _ERROR_MAP = getErrors();

	public static Message createErrorMsg(String from, String to, String text, String code) {
		Message msg = new Message();
		if (StringUtils.isNotBlank(from)) {
			msg.setFrom(from);
		}
		msg.setTo(to);
		msg.setType(Message.Type.error);
		Element root = msg.getElement();
		Element error = root.addElement("error");
		completeErrorElement(error, text, code);
		return msg;
	}

	public static Message createErrorMsg(JID from, JID to, String text, String code) {
		Message msg = new Message();
		if (null != from) {
			msg.setFrom(from);
		}
		msg.setTo(to);
		msg.setType(Message.Type.error);
		Element root = msg.getElement();
		Element error = root.addElement("error");
		completeErrorElement(error, text, code);
		return msg;
	}

	public static Element appendErrorChildToIQ(IQ fromIQ, IQ replyIQ, String code, String text) {
		replyIQ.setType(IQ.Type.error);
		Element error = DocumentHelper.createElement("error");
		completeErrorElement(error, text, code);
		replyIQ.setChildElement(error);
		return error;
	}

	protected static void completeErrorElement(Element error, String text, String code) {

		error.addAttribute("type", "cancel");

		Element serviceUnavailable = error.addElement("service-unavailable");
		serviceUnavailable.addAttribute("xmlns", "urn:ietf:params:xml:ns:xmpp-stanzas");

		Element textEle = serviceUnavailable.addElement("text");
		textEle.addAttribute("xmlns", "urn:ietf:params:xml:ns:xmpp-stanzas");
		textEle.setText(text);

		Element codeEle = serviceUnavailable.addElement("code", "http://www.servyou.com.cn/xmpp/extension");
		codeEle.setText(code);
	}

	private static Map<String, String> getErrors() {
		MessageXmlDomReader messageXmlReader = null;
		messageXmlReader = new MessageXmlDomReader(ConfigUtils.getFile("error_message.xml").getAbsolutePath());
		Map<String, String> map = messageXmlReader.parserXml();
		return map;
	}

	public static String getErrorText(String code) {
		String text = _ERROR_MAP.get(code);
		if (StringUtils.isBlank(text)) {
			text = "";
		}
		return text;
	}

	@SuppressWarnings("rawtypes")
	public static Result getResult(String code, Object... args) {
		return new DefaultResult(new net.yanrc.app.common.error.Message(code,
				String.format(_ERROR_MAP.get(code), args)));
	}

	public static Head getHead(String code, Object... args) {
		return new Head(code, String.format(_ERROR_MAP.get(code), args));
	}

}
