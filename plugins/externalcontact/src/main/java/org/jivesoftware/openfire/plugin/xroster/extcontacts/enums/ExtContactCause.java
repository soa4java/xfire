package org.jivesoftware.openfire.plugin.xroster.extcontacts.enums;

import org.apache.commons.lang.StringUtils;

public enum ExtContactCause {
    /**
     * 会话结束原因
     * 0:表示服务端检测到会话断开自动发送的
     * 1:表示通过协议断开
     */
    SERVER_DETECTION("1"),
    PROTOCOL("0");

    private String code;

    ExtContactCause(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ExtContactCause fromCode(String code) {
        for (ExtContactCause cause : ExtContactCause.values()) {
            if (StringUtils.equalsIgnoreCase(cause.code, code)) {
                return cause;
            }
        }
        throw new IllegalArgumentException(String.format("Unsupported EcCause：%s.", code));
    }

    public boolean is(String code) {
        return StringUtils.equalsIgnoreCase(this.code, code);
    }

    @Override
    public String toString() {
        return code;
    }
}

