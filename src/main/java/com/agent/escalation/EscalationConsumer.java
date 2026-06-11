package com.agent.escalation;

import com.agent.common.constant.RabbitConstant;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 转人工消息消费者。
 *
 * <p>演示：收到消息后打印日志、模拟通知坐席。手动 ACK，处理失败则进入死信队列。
 * 真实场景可在此把会话分配给在线坐席、推送 WebSocket 通知等。
 */
@Slf4j
@Component
public class EscalationConsumer {

    @RabbitListener(queues = RabbitConstant.ESCALATION_QUEUE)
    public void onEscalation(String payload, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("【坐席通知】收到转人工消息: {}", payload);
            // 演示实现：分配坐席并通知（真实场景可在此推送 WebSocket / 写坐席工作台）
            notifyAgent(payload);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("转人工消息处理失败，拒绝并投递死信 payload={}", payload, e);
            // 不重回队列，转入死信队列由人工排查
            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * 模拟通知坐席。
     */
    private void notifyAgent(String payload) {
        log.info("已将会话分配给在线坐席，携带上下文: {}", payload);
    }

    @RabbitListener(queues = RabbitConstant.ESCALATION_DLQ)
    public void onDeadLetter(String payload) {
        log.error("【死信】转人工消息进入死信队列，需人工介入: {}", payload);
    }
}
