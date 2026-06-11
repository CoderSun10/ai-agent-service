package com.agent.agent;

import com.agent.common.enums.IntentType;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.message.AiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 意图识别 Agent。
 *
 * <p>用一次轻量 LLM 调用，把用户输入分类为 KNOWLEDGE / TOOL / CHAT，并给出置信度。
 * 输出强制 JSON，便于结构化解析；解析失败时回退为 CHAT。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntentAgent {

    private final ChatLanguageModel chatModel;

    private static final String SYSTEM_PROMPT = """
            你是一个客服意图分类器。请将用户最新的输入分类为以下三类之一：
            - knowledge：用户在询问产品政策、规则、使用方法等需要查知识库的问题（如退换货政策、保修期、会员权益）。
            - tool：用户要求执行具体业务操作，需要查订单/物流或发起退款（如"我的订单到哪了""我要退款""帮我查订单 O123"）。
            - chat：闲聊、问候、与业务无关的通用对话。

            只输出一个 JSON，不要任何额外文字，格式：
            {"intent":"knowledge|tool|chat","confidence":0.0~1.0}
            其中 confidence 表示你对该分类的把握程度。
            """;

    /**
     * 分类用户意图。
     *
     * @param userMessage 用户最新输入
     * @param history     历史消息（提供上下文，可为空）
     */
    public IntentResult classify(String userMessage, List<ChatMessage> history) {
        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(SystemMessage.from(SYSTEM_PROMPT));
            // 仅取最近 2 轮历史作为上下文，避免干扰
            int from = Math.max(0, history.size() - 4);
            messages.addAll(history.subList(from, history.size()));
            messages.add(UserMessage.from(userMessage));

            Response<AiMessage> response = chatModel.generate(messages);
            String text = response.content().text().trim();
            return parse(text);
        } catch (Exception e) {
            log.warn("意图识别失败，回退为 CHAT: {}", e.getMessage());
            return IntentResult.chat();
        }
    }

    private IntentResult parse(String text) {
        String json = extractJson(text);
        try {
            JSONObject obj = JSON.parseObject(json);
            IntentType type = IntentType.parse(obj.getString("intent"));
            Double rawConfidence = obj.getDouble("confidence");
            double confidence = rawConfidence == null ? 0.6 : rawConfidence;
            confidence = Math.max(0.0, Math.min(1.0, confidence));
            return new IntentResult(type, confidence, text);
        } catch (Exception e) {
            log.warn("意图 JSON 解析失败 raw={}", text);
            return new IntentResult(IntentType.parse(text), 0.5, text);
        }
    }

    /**
     * 从可能含多余文字的输出中提取首个 JSON 对象。
     */
    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }
}
