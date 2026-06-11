package com.agent.agent;

import com.agent.common.enums.IntentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 意图识别结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntentResult {

    private IntentType type;

    /** 分类置信度 0~1 */
    private double confidence;

    /** 模型原始输出（调试用） */
    private String rawText;

    public boolean isKnowledgeQuery() {
        return type == IntentType.KNOWLEDGE;
    }

    public boolean isToolCall() {
        return type == IntentType.TOOL;
    }

    public static IntentResult chat() {
        return new IntentResult(IntentType.CHAT, 0.5, "fallback-chat");
    }
}
