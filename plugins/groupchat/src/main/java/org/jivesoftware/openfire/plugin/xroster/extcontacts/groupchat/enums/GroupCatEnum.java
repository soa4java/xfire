package org.jivesoftware.openfire.plugin.xroster.extcontacts.groupchat.enums;

public enum GroupCatEnum {
	DISCUSS_GROUP("讨论组", "discuss_group"), //
	TEMP("临时组", "temp"), //
	COMMUNITY("community", "community"), //
	PRIVATE_GROUP("private_group", "private_group");//

	GroupCatEnum(String text, String value) {
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

}
