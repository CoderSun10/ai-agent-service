package com.agent.common.enums;

import lombok.Getter;

/**
 * 消息角色。
 */
@Getter
public enum MessageRole {

    USER("user"),
    ASSISTANT("assistant"),
    TOOL("tool");

    private final String value;

    MessageRole(String value) {
        this.value = value;
    }
}
