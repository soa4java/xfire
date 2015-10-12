package sitong.thinker.of.plugin.groupchat.enums;

public enum InviteTypeEnum {
	
	EXPLICITINVITE("显示","1"),  INLICIT("隐式","0");

	InviteTypeEnum(String text, String value) {
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
