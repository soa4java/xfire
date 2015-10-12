package org.jivesoftware.of.common.enums;


public enum JidResourceEnum {
	/**
	 * 资源
	 * web: web
	 * pc: ERC
	 * mbc: MBC (表示移动端:Mobile connection,包含了Android和ios)
	 * android: android
	 * ios: ios
	 */
	WEB("web", "web", 0),
	PC("pc", "ERC", 10),
	MBC("mbc", "MBC", 6),
	ANDROID("android", "android", 5),
	IOS("ios", "ios", 4),
	OTHER("other", "other", -1);

	JidResourceEnum(String text, String value, int level) {
		this.text = text;
		this.value = value;
		this.level = level;
	}

	private String text;
	private String value;

	private int level;

	public String getText() {
		return text;
	}

	public String getValue() {
		return value;
	}

	public int getLevel() {
		return level;
	}

	public static JidResourceEnum fromValue(String value) {
        for (JidResourceEnum enumObj : JidResourceEnum.values()) {
            if (enumObj.value.equalsIgnoreCase(value)) {
                return enumObj;
            }
        }
        return null;
    }


	public static boolean isMobile(String value) {
		return ANDROID.value.equalsIgnoreCase(value) || IOS.value.equalsIgnoreCase(value) || MBC.value.equalsIgnoreCase(value);

	}

}
