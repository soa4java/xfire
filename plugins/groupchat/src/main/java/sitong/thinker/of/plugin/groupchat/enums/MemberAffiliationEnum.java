package sitong.thinker.of.plugin.groupchat.enums;

public enum MemberAffiliationEnum {

	OUTCAST("被排除在外", "outcast"), NONE("普通用户", "none"), MEMBER("成员", "member"), ADMIN("管理员", "admin"), OWNER("拥有者",
			"owner");

	MemberAffiliationEnum(String text, String value) {
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
