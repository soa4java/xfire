package org.jivesoftware.of.common.enums;

import org.apache.commons.lang.StringUtils;

public enum ShowStatusEnum {
	EMPTY("", "");

	ShowStatusEnum(String text, String value) {
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

	public static ShowStatusEnum fromValue(String value) {
		for (ShowStatusEnum enumObj : ShowStatusEnum.values()) {
			if (StringUtils.equalsIgnoreCase(enumObj.value, value)) {
				return enumObj;
			}
		}
		return null;
	}

}
