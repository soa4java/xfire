package org.jivesoftware.of.common.enums;

import org.apache.commons.lang.StringUtils;

public enum NetStatusEnum {
	PC("PC", "PC"), G2("2G", "2G"), G3("3G", "3G"), WIFI("wifi", "wifi");

	NetStatusEnum(String text, String value) {
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

	public static NetStatusEnum fromValue(String value) {
		for (NetStatusEnum enumObj : NetStatusEnum.values()) {
			if (StringUtils.equalsIgnoreCase(enumObj.value, value)) {
				return enumObj;
			}
		}
		return null;
	}

}
