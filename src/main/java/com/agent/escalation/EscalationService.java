package com.agent.escalation;

import com.agent.common.constant.RabbitConstant;
import com.agent.common.enums.ConversationStatus;
import com.agent.entity.OutboxMessage;
import com.agent.mapper.ConversationMapper;
import com.agent.mapper.OutboxMessageMapper;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;

/**
 * 转人工服务 + Outbox 模式。
 *
 * <p>核心：会话状态更新与「待发送消息」写入在同一事务内（原子），
 * 再由定时任务异步投递 RabbitMQ，避免「DB 提交成功但 MQ 发送失败导致消息丢失」。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EscalationService {

    private final OutboxMessageMapper outboxMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ConversationMapper conversationMapper;

    private static final int FLUSH_BATCH = 50;

    /**
     * 触发转人工：更新会话状态 + 写 Outbox（同一事务）。
     */
    @Transactional
    public void escalate(String convId, Long userId, String reason) {
        conversationMapper.updateStatus(convId, ConversationStatus.ESCALATED.getCode());
        conversationMapper.markConfidenceLow(convId);

        OutboxMessage msg = new OutboxMessage();
        msg.setConversationId(convId);
        msg.setPayload(JSON.toJSONString(Map.of(
                "convId", convId,
                "userId", userId,
                "reason", reason == null ? "低置信自动转人工" : reason,
                "timestamp", System.currentTimeMillis()
        )));
        msg.setStatus(0);
        msg.setRetryCount(0);
        outboxMapper.insert(msg);

        log.info("会话已转人工 convId={} userId={} reason={}", convId, userId, reason);
    }

    /**
     * 定时扫描 Outbox 未发送消息，投递到 RabbitMQ。
     */
    @Scheduled(fixedDelay = 3000)
    public void flushOutbox() {
        List<OutboxMessage> pending = outboxMapper.selectPending(FLUSH_BATCH);
        if (pending.isEmpty()) {
            return;
        }
        for (OutboxMessage msg : pending) {
            try {
                rabbitTemplate.convertAndSend(
                        RabbitConstant.ESCALATION_EXCHANGE,
                        RabbitConstant.ESCALATION_ROUTING_KEY,
                        msg.getPayload());
                outboxMapper.markSent(msg.getId());
                log.debug("Outbox 投递成功 id={}", msg.getId());
            } catch (Exception e) {
                outboxMapper.incrRetry(msg.getId());
                log.warn("Outbox 投递失败，等待重试 id={} err={}", msg.getId(), e.getMessage());
            }
        }
    }
}
