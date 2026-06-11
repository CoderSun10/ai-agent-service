package com.agent.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.agent.common.Result;
import com.agent.orchestrator.AgentOrchestrator;
import com.agent.service.ConversationService;
import com.agent.service.MessageService;
import com.agent.vo.ConversationVO;
import com.agent.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 对话接口（SSE 流式）。
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AgentOrchestrator orchestrator;
    private final ConversationService conversationService;
    private final MessageService messageService;

    /**
     * 新建会话。
     */
    @PostMapping("/conversation")
    public Result<String> newConversation() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(conversationService.create(userId));
    }

    /**
     * 会话列表。
     */
    @GetMapping("/conversations")
    public Result<List<ConversationVO>> conversations() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(conversationService.listByUser(userId));
    }

    /**
     * 历史消息。
     */
    @GetMapping("/history/{convId}")
    public Result<List<MessageVO>> history(@PathVariable String convId) {
        Long userId = StpUtil.getLoginIdAsLong();
        conversationService.requireOwned(convId, userId);
        return Result.success(messageService.listByConvId(convId));
    }

    /**
     * 流式对话接口。
     * GET /api/chat/stream?conversationId=xxx&message=你好
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(required = false) String conversationId,
                             @RequestParam String message) {

        Long userId = StpUtil.getLoginIdAsLong();
        // 会话不存在则新建，存在则校验归属
        String convId = conversationService.createIfAbsent(conversationId, userId);

        SseEmitter emitter = new SseEmitter(60_000L);
        emitter.onTimeout(() -> log.warn("SSE 超时 convId={}", convId));
        emitter.onError(e -> log.error("SSE 异常 convId={}", convId, e));

        // 虚拟线程处理（Java 21），避免阻塞 Tomcat 工作线程
        Thread.ofVirtual().name("sse-chat-" + convId).start(() -> {
            try {
                // 先把 convId 回传给前端
                emitter.send(SseEmitter.event().name("conversation").data(convId));
                orchestrator.chat(convId, userId, message, emitter);
            } catch (Exception e) {
                log.error("对话处理失败 convId={}", convId, e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("服务异常: " + e.getMessage()));
                } catch (Exception ignored) {
                    // 连接可能已断开
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
