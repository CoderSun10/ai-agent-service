package com.agent.common.util;

/**
 * 简易 Token 估算工具。
 *
 * <p>未引入精确的 tiktoken，采用经验近似：
 * <ul>
 *   <li>中文：约 1 字 ≈ 1 token</li>
 *   <li>英文/数字/符号：约 4 字符 ≈ 1 token</li>
 * </ul>
 * 用于滑动窗口裁剪，无需绝对精确。
 */
public final class TokenCounter {

    private TokenCounter() {
    }

    public static int estimate(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int cjk = 0;
        int other = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isCjk(c)) {
                cjk++;
            } else {
                other++;
            }
        }
        return cjk + (int) Math.ceil(other / 4.0);
    }

    private static boolean isCjk(char c) {
        return c >= 0x4E00 && c <= 0x9FFF;
    }
}
