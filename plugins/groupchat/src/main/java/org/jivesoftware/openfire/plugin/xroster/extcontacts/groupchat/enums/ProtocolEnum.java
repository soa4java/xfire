package org.jivesoftware.openfire.plugin.xroster.extcontacts.groupchat.enums;

public enum ProtocolEnum {
	IQ_CREATE_GROUP("http://www.servyou.com.cn/protocols/gc#create-group"),//创建组
	IQ_GROUP_CHAT("http://www.servyou.com.cn/protocol/gc"),//
	IQ_DISCO_INFO("http://jabber.org/protocol/disco#info"), //
	IQ_GET_GROUPS_EXISTS("http://www.servyou.com.cn/protocols/gc#group-exists"),//
	IQ_GET_CHAT_GROUPS("http://www.servyou.com.cn/protocols/gc#get-chat-groups"),//获取和用户相关的聊天组
	IQ_GET_GROUP_MEMBERS("http://www.servyou.com.cn/protocols/gc#get-members"),//获取组成员
	IQ_UPDATE_GROUP_INFO("http://www.servyou.com.cn/protocols/gc#update-group-info"),//更新组信息
	IQ_INVITE_MEMBERS("http://www.servyou.com.cn/protocols/gc#invite-members"),//发起邀请
	MSG_IS_INVITED("http://www.servyou.com.cn/protocols/gc#is-accepted-invitation"),//询问用户是否接受邀请
	MSG_NEW_MEMBERS("http://www.servyou.com.cn/protocols/gc#new-members"),//增加新成员
	CANCEL_MEMBER("http://www.servyou.com.cn/protocols/gc#cancel-member")//退出聊天组
	;

	ProtocolEnum(String url) {
		this.url = url;
	}

	private String url;

	public String getUrl() {
		return url;
	}

}
