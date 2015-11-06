package org.jivesoftware.openfire.plugin.xroster.groupchat.enums;

public enum MemberRoleEnum {

	NONE("none", "none"), VISIT("访客", "visitor"), PARTICIPANT("成员", "participant"), MODERATOR("持有者", "moderator");

	MemberRoleEnum(String text, String value) {
		this.text = text;
		this.value = value;
	}

	private String text;
	private String value;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	// member

}
