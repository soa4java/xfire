package org.jivesoftware.of.common.enums;



import org.apache.commons.lang.StringUtils;

public enum ReceiverType {
    PERSON("0"),
    DEPARTMENT("1"),
    COMPANY("2");

    ReceiverType(String code) {
        this.code = code;
    }

    private String code;


    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ReceiverType fromCode(String code) {
        for (ReceiverType receiverType : ReceiverType.values()) {
            if (StringUtils.equalsIgnoreCase(receiverType.code, code)) {
                return receiverType;
            }
        }
        throw new IllegalArgumentException(String.format("Unsupported receiver type:%s.", code));
    }

    @Override
    public String toString() {
        return code;
    }
}

