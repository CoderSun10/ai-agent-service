package com.agent.agent;

import com.agent.common.enums.IntentType;
import com.agent.tool.LogisticsTool;
import com.agent.tool.OrderQueryTool;
import com.agent.tool.RefundTool;
import com.agent.tool.WorkOrderTool;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 回复生成 Agent：基于 StreamingChatLanguageModel 流式输出，并挂载业务工具（Tool Calling）。
 *
 * <p>通过 LangChain4j AiServices 自动完成「LLM 请求 → 工具调用 → 工具结果回灌 → 续写」的循环，
 * 业务工具以 @Tool 注解的 Bean 形式注入。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReplyAgent {

    private final StreamingChatLanguageModel streamingChatModel;
    private final OrderQueryTool orderQueryTool;
    private final LogisticsTool logisticsTool;
    private final RefundTool refundTool;
    private final WorkOrderTool workOrderTool;

    /** 保存本线程最近一次回复的置信度（与 generateStream 返回值一致，兼容旧接口）。 */
    private final ThreadLocal<Double> lastConfidence = ThreadLocal.withInitial(() -> 0.8);

    /** AiServices 定义的流式助手接口。 */
    interface Assistant {
        TokenStream chat(String userMessage);
    }

    /**
     * 流式生成回复。阻塞直到流结束（调用方运行在独立虚拟线程中）。
     *
     * @param userMessage      用户输入
     * @param history          历史消息
     * @param retrievedContext RAG 检索到的上下文（可为空）
     * @param intent           意图结果
     * @param tokenConsumer    每个增量 token 的回调
     * @return 本次回复的综合置信度
     */
    public double generateStream(String userMessage,
                                 List<ChatMessage> history,
                                 String retrievedContext,
                                 IntentResult intent,
                                 Consumer<String> tokenConsumer) {

        MessageWindowChatMemory memory = MessageWindowChatMemory.withMaxMessages(40);
        memory.add(SystemMessage.from(buildSystemPrompt(intent, retrievedContext)));
        history.forEach(memory::add);

        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatLanguageModel(streamingChatModel)
                .chatMemory(memory)
                .tools(orderQueryTool, logisticsTool, refundTool, workOrderTool)
                .build();

        StringBuilder full = new StringBuilder();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        TokenStream stream = assistant.chat(userMessage);
        stream.onNext(token -> {
                    full.append(token);
                    tokenConsumer.accept(token);
                })
                .onComplete((Response<AiMessage> resp) -> latch.countDown())
                .onError(err -> {
                    error.set(err);
                    latch.countDown();
                })
                .start();

        try {
            // 最长等待 60s，与 SSE 超时对齐
            if (!latch.await(60, TimeUnit.SECONDS)) {
                log.warn("回复生成超时 userMessage={}", userMessage);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (error.get() != null) {
            log.error("流式回复异常", error.get());
            throw new RuntimeException("回复生成失败: " + error.get().getMessage(), error.get());
        }

        double confidence = computeConfidence(intent, retrievedContext, full.toString());
        lastConfidence.set(confidence);
        return confidence;
    }

    /**
     * 返回本线程最近一次回复的置信度。
     */
    public double lastConfidence() {
        return lastConfidence.get();
    }

    /**
     * 构建系统提示词：注入角色设定、意图、RAG 上下文与转人工规则。
     */
    private String buildSystemPrompt(IntentResult intent, String retrievedContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一名专业、友好的电商智能客服助手。回答要简洁、准确、有礼貌。\n");
        sb.append("当用户需要查询订单、物流或申请退款时，请调用提供的工具获取真实数据，不要编造。\n");
        sb.append("当你无法确定答案、或问题超出能力范围时，请如实说明并建议转接人工客服。\n");

        if (intent.getType() == IntentType.KNOWLEDGE) {
            sb.append("\n以下是从知识库检索到的资料，请优先依据这些资料回答；");
            sb.append("若资料不足以回答，请明确说明而不要臆测：\n");
            sb.append("====== 知识库资料 ======\n");
            sb.append(retrievedContext == null || retrievedContext.isBlank()
                    ? "（未检索到相关资料）" : retrievedContext);
            sb.append("\n========================\n");
        }
        return sb.toString();
    }

    /**
     * 置信度启发式评估：
     * <ul>
     *   <li>知识库问答但未检索到资料 → 降低置信</li>
     *   <li>回复包含不确定/转人工措辞 → 降低置信</li>
     *   <li>回复过短 → 略微降低</li>
     * </ul>
     */
    private double computeConfidence(IntentResult intent, String retrievedContext, String reply) {
        double score = intent.getConfidence() <= 0 ? 0.8 : Math.max(0.6, intent.getConfidence());

        if (intent.getType() == IntentType.KNOWLEDGE
                && (retrievedContext == null || retrievedContext.isBlank())) {
            score -= 0.35;
        }

        String lower = reply == null ? "" : reply;
        String[] uncertainMarks = {"无法", "不确定", "抱歉", "转人工", "人工客服", "不清楚", "联系客服"};
        for (String mark : uncertainMarks) {
            if (lower.contains(mark)) {
                score -= 0.2;
                break;
            }
        }

        if (lower.length() < 5) {
            score -= 0.1;
        }

        return Math.max(0.0, Math.min(1.0, score));
    }
}
