package org.jivesoftware.openfire.plugin.xroster.extcontacts.component;

import org.jivesoftware.of.common.constants.XConstants;


public enum ExtContactProtocalEnum {

	IQ_EC_URI("http://www.servyou.com.cn/protocol/ec"), //
	IQ_DISCO_INFO("http://jabber.org/protocol/disco#info"), //
	IQ_LIST_CONTACTS_URI("http://www.servyou.com.cn/protocol/ec#contact/list"), //
	IQ_JOIN_QUEUE_URI("http://www.servyou.com.cn/protocol/ec#contact/join-queue"), //
	IQ_CANCEL_QUEUE("http://www.servyou.com.cn/protocol/ec#contact/cancel-queue"), //
	GET_CUR_POS_URI("http://www.servyou.com.cn/protocol/ec#group/get-cur-pos"), //
	MSG_SESSION_CREATE_FOR_CONSULTER_URI(XConstants.EC_CREATE_FOR_CONSULT), //
	MSG_SESSION_CREATE_FOR_AGENT_URI(XConstants.EC_CREATE_FOR_AGENT), //
	MSG_SESSION_CHAT_MSG_TEXT_URI(XConstants.EC_CHAT), //
	MSG_SESSION_CHAT_MSG_FILE_URI(XConstants.EC_FILE), //
	MSG_LIST_CONTACTS_CHANGE_URI("http://www.servyou.com.cn/protocol/ec#contact/change"), //
	IQ_SESSION_EXISTS_CHECK("http://www.servyou.com.cn/protocol/ec#session/exists-ckeck"), //
	IQ_LIST_CHAT_MSGS_URI("http://www.servyou.com.cn/protocol/ec#chat/list"), //
	MSG_SESSION_REQUEST_CLOSE_URI(XConstants.EC_REQ_CLOSE), //
	CLOSE_SESSION_URI(XConstants.EC_SESSION_CLOSE), //

	IQ_GET_IDLE_GROUPS_URI("http://www.servyou.com.cn/protocol/ec#idle-groups/get"), //
	MSG_SWITCH_URI("http://www.servyou.com.cn/protocol/ec#switch/created-for-consult"), //
	MSG_COLLABORATION_URI("http://www.servyou.com.cn/protocol/ec#collaboration/created-for-consult");//

	private String uri;

	ExtContactProtocalEnum(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the description
	 */
	public String getUri() {
		return uri;
	}

	public boolean is(String uri) {
		return getUri().equalsIgnoreCase(uri);
	}

}
