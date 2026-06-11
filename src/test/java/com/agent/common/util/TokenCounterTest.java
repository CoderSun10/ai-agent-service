package com.agent.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TokenCounter 单元测试（纯逻辑，不依赖 Spring 上下文）。
 */
class TokenCounterTest {

    @Test
    void emptyOrNull() {
        assertEquals(0, TokenCounter.estimate(null));
        assertEquals(0, TokenCounter.estimate(""));
    }

    @Test
    void chineseRoughlyOnePerChar() {
        // 4 个汉字 ≈ 4 token
        assertEquals(4, TokenCounter.estimate("你好世界"));
    }

    @Test
    void englishRoughlyQuarter() {
        // 8 个英文字符 ≈ 2 token
        assertEquals(2, TokenCounter.estimate("abcdefgh"));
    }

    @Test
    void mixed() {
        int t = TokenCounter.estimate("订单 O20260601001 物流");
        assertTrue(t > 0);
    }
}
