package com.agent.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * IntentType 容错解析测试。
 */
class IntentTypeTest {

    @Test
    void parseExact() {
        assertEquals(IntentType.KNOWLEDGE, IntentType.parse("knowledge"));
        assertEquals(IntentType.TOOL, IntentType.parse("tool"));
        assertEquals(IntentType.CHAT, IntentType.parse("chat"));
    }

    @Test
    void parseWithNoiseAndCase() {
        assertEquals(IntentType.TOOL, IntentType.parse("  TOOL  "));
        assertEquals(IntentType.KNOWLEDGE, IntentType.parse("intent=Knowledge"));
    }

    @Test
    void parseFallbackToChat() {
        assertEquals(IntentType.CHAT, IntentType.parse(null));
        assertEquals(IntentType.CHAT, IntentType.parse("unknown-xyz"));
    }
}
