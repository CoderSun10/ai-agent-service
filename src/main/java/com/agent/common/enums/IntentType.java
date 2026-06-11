package com.agent.common.enums;

import lombok.Getter;

/**
 * 用户意图类型。
 */
@Getter
public enum IntentType {

    KNOWLEDGE("knowledge", "知识库问答，需要走 RAG 检索"),
    TOOL("tool", "业务操作，需要调用工具（订单/物流/退款）"),
    CHAT("chat", "闲聊/通用对话，直接回复");

    private final String value;
    private final String desc;

    IntentType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 容错解析：大小写、含子串均可匹配，默认 CHAT。
     */
    public static IntentType parse(String raw) {
        if (raw == null) {
            return CHAT;
        }
        String lower = raw.trim().toLowerCase();
        for (IntentType t : values()) {
            if (lower.contains(t.value)) {
                return t;
            }
        }
        return CHAT;
    }
}
