package org.jivesoftware.of.common.enums;

public enum Resource {
	/**
	 * 资源
	 * web: web
	 * pc: ERC
	 * mbc: MBC (表示移动端:Mobile connection,包含了Android和ios)
	 * android: android
	 * ios: ios
	 */
	WEB("web", "web", 0), PC("pc", "ERC", 10), MBC("mbc", "MBC", 6), ANDROID("android", "android", 5), IOS("ios",
			"ios", 4), OTHER("other", "other", -1);

	private String text;
	private String code;
	private Integer level;

	Resource(String text, String code, Integer level) {
		this.text = text;
		this.code = code;
		this.level = level;
	}

	public static Resource fromCode(String code) {
		for (Resource resource : Resource.values()) {
			if (resource.code.equalsIgnoreCase(code)) {
				return resource;
			}
		}
		throw new IllegalArgumentException(String.format("Unsupported resource：%s.", code));
	}

	public static boolean isMobile(String value) {
		return ANDROID.code.equalsIgnoreCase(value) || IOS.code.equalsIgnoreCase(value)
				|| MBC.code.equalsIgnoreCase(value);

	}

	public String getText() {
		return text;
	}

	public String getCode() {
		return code;
	}

	public Integer getLevel() {
		return level;
	}

	public boolean is(String code) {
		return this.text.equalsIgnoreCase(code);
	}

	@Override
	public String toString() {
		return code;
	}
}
