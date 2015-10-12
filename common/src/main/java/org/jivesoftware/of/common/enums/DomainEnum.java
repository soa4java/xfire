package org.jivesoftware.of.common.enums;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.util.JiveGlobals;

public enum DomainEnum {
	SERVYOU("im", "im"), SERVYOU3("im3", "im3"), DEFAULT(JiveGlobals.getProperty("xmpp.domain", "im").toLowerCase(), JiveGlobals.getProperty("xmpp.domain", "im").toLowerCase());

	DomainEnum(String text, String value) {
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

	public static DomainEnum fromValue(String value) {
		for (DomainEnum enumObj : DomainEnum.values()) {
			if (StringUtils.equalsIgnoreCase(enumObj.value, value)) {
				return enumObj;
			}
		}
		return null;
	}

}
