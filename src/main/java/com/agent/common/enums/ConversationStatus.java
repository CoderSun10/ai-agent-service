package com.agent.common.enums;

import lombok.Getter;

/**
 * 会话状态。
 */
@Getter
public enum ConversationStatus {

    IN_PROGRESS(0, "进行中"),
    ENDED(1, "已结束"),
    ESCALATED(2, "已转人工");

    private final int code;
    private final String desc;

    ConversationStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ConversationStatus of(int code) {
        for (ConversationStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return IN_PROGRESS;
    }
}
