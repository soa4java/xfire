package sitong.thinker.of.plugin.groupchat.enums;

import org.apache.commons.lang.StringUtils;

public enum GroupTypeEnum {
	PERSIATENT("永久组", "persistent"), TEMPORARY("临时组", "temporary"), UNKNOWN("未知类型组", "unknown");

	GroupTypeEnum(String text, String value) {
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

	public static String getValueByCat(String groupCat) {
		if (StringUtils.isNotBlank(groupCat) && groupCat.equals(GroupCatEnum.DISCUSS_GROUP.getValue())) {
			return PERSIATENT.getValue();
		} else if (StringUtils.isNotBlank(groupCat) && groupCat.equals(GroupCatEnum.TEMP.getValue())) {
			return TEMPORARY.getValue();
		} else {
			return "";
		}
	}
}
