package com.agent.orchestrator;

import com.agent.agent.IntentAgent;
import com.agent.agent.IntentResult;
import com.agent.agent.ReplyAgent;
import com.agent.agent.RetrievalAgent;
import com.agent.escalation.EscalationService;
import com.agent.memory.RedisConversationMemory;
import com.agent.service.MessageService;
import dev.langchain4j.data.message.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

/**
 * 多 Agent 编排核心。
 *
 * <p>链路：加载历史 → 意图识别 → （知识类）RAG 检索 → 流式回复 + Tool Calling
 * → 持久化 → 置信度评估 → （低置信）转人工。
 * 根据意图按需调度子 Agent，避免无意义的 LLM 调用。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestrator {

    private final IntentAgent intentAgent;
    private final RetrievalAgent retrievalAgent;
    private final ReplyAgent replyAgent;
    private final EscalationService escalationService;
    private final RedisConversationMemory memory;
    private final MessageService messageService;

    @Value("${agent.escalation.confidence-threshold:0.6}")
    private double confidenceThreshold;

    /**
     * 处理一轮对话，结果通过 SSE 流式推送。
     */
    public void chat(String convId, Long userId, String userMessage, SseEmitter emitter) throws IOException {
        log.info("编排开始 convId={} userId={} msg={}", convId, userId, userMessage);

        // 1. 加载历史
        List<ChatMessage> history = memory.loadHistory(convId);

        // 2. 意图识别
        IntentResult intent = intentAgent.classify(userMessage, history);
        emitter.send(SseEmitter.event().name("intent").data(intent.getType().getValue()));
        log.info("意图识别 convId={} type={} conf={}", convId, intent.getType(), intent.getConfidence());

        // 3. 知识检索（仅知识库问答类走 RAG）
        String retrievedContext = "";
        if (intent.isKnowledgeQuery()) {
            retrievedContext = retrievalAgent.retrieve(userMessage);
        }

        // 4. 流式回复（含 Tool Calling）
        StringBuilder fullReply = new StringBuilder();
        double confidence = replyAgent.generateStream(
                userMessage, history, retrievedContext, intent,
                token -> {
                    try {
                        emitter.send(SseEmitter.event().name("message").data(token));
                        fullReply.append(token);
                    } catch (IOException e) {
                        throw new RuntimeException("SSE 推送失败", e);
                    }
                });

        // 5. 持久化：Redis 短期记忆 + MySQL 落库
        String reply = fullReply.toString();
        memory.save(convId, userMessage, reply);
        messageService.savePair(convId, userMessage, reply);

        // 6. 置信度评估 → 转人工
        log.info("回复完成 convId={} confidence={}", convId, confidence);
        if (confidence < confidenceThreshold) {
            escalationService.escalate(convId, userId, "回复置信度过低: " + confidence);
            emitter.send(SseEmitter.event().name("escalate")
                    .data("您的问题已为您转接人工客服，请稍候。"));
        }

        emitter.send(SseEmitter.event().name("done").data("[DONE]"));
        emitter.complete();
        log.info("编排结束 convId={}", convId);
    }
}
