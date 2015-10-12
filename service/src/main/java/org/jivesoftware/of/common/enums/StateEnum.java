package org.jivesoftware.of.common.enums;

public enum StateEnum {

	DEFAULT("", "", 0), // 默认的
	INVAL("inval", "离线", 1), //
	AWAY("away", "离开", 3), //
	AVAIL("avail", "在线", 5);//

	StateEnum(String code, String text, int level) {
		this.code = code;
		this.text = text;
		this.level = level;
	}

	private String code;
	private String text;
	private int level;

	public String text() {
		return text;
	}

	public String code() {
		return code;
	}

	public int getLevel() {
		return level;
	}

	public static StateEnum fromCode(String code) {
		for (StateEnum enumObj : StateEnum.values()) {
			if (enumObj.code.equalsIgnoreCase(code)) {
				return enumObj;
			}
		}
		return null;
	}

}
