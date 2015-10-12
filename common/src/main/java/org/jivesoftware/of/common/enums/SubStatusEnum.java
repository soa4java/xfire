package org.jivesoftware.of.common.enums;

import org.apache.commons.lang.StringUtils;

public enum SubStatusEnum {
    INIT("0", "初始状态"), VALID("1", "有效"), INVAL("2", "无效");

    private String code;
    private String value;
    SubStatusEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public static SubStatusEnum fromValue(String value) {
        for (SubStatusEnum enumObj : SubStatusEnum.values()) {
            if (StringUtils.equalsIgnoreCase(enumObj.value, value)) {
                return enumObj;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
