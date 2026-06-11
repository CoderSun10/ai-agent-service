package com.agent.common.constant;

import java.time.Duration;

/**
 * Redis key 模板与 TTL 常量。
 */
public final class RedisKey {

    private RedisKey() {
    }

    /** 对话历史（List） */
    private static final String CONVERSATION_HISTORY = "conversation:history:%s";

    /** 对话 Token 计数（String） */
    private static final String CONVERSATION_TOKENS = "conversation:tokens:%s";

    /** 用户在线会话映射（String） */
    private static final String USER_CONVERSATION = "user:conversation:%s";

    /** 历史 / Token TTL：7 天 */
    public static final Duration HISTORY_TTL = Duration.ofDays(7);

    /** 用户会话映射 TTL：1 天 */
    public static final Duration USER_CONVERSATION_TTL = Duration.ofDays(1);

    public static String history(String conversationId) {
        return String.format(CONVERSATION_HISTORY, conversationId);
    }

    public static String tokens(String conversationId) {
        return String.format(CONVERSATION_TOKENS, conversationId);
    }

    public static String userConversation(Long userId) {
        return String.format(USER_CONVERSATION, userId);
    }
}
