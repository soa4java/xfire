package org.jivesoftware.of.common.enums;

import org.apache.commons.lang.StringUtils;

public enum StateEnum {

    DEFAULT("", "", 0),// 默认的
    INVAL("inval", "离线", 1),//
    AWAY("away", "离开", 3),//
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

    public static PresenceStatus fromCode(String code) {
        for (PresenceStatus enumObj : PresenceStatus.values()) {
            if (StringUtils.equalsIgnoreCase(enumObj.code(), code)) {
                return enumObj;
            }
        }
        return null;
    }

}
