package org.jivesoftware.openfire.plugin.xroster.extcontacts.enums;

import org.apache.commons.lang.StringUtils;


public enum ExtContactNextAction {
    /**
     * none：没有下一步操作
     * switch：在转接
     * collaboration：求助协助中
     */
    NONE("none"),
    SWITCH("switch"),
    COLLABORATION("collaboration");

    private String code;

    ExtContactNextAction(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ExtContactNextAction fromCode(String code) {
        for (ExtContactNextAction nextAction : ExtContactNextAction.values()) {
            if (StringUtils.equalsIgnoreCase(nextAction.code, code)) {
                return nextAction;
            }
        }
        throw new IllegalArgumentException(String.format("Unsupported EcNextAction：%s.", code));
    }

    public boolean is(String code) {
        return StringUtils.equalsIgnoreCase(this.code, code);
    }

    @Override
    public String toString() {
        return code;
    }
}

