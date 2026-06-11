package com.agent.config;

import com.agent.common.constant.RabbitConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * RabbitMQ 队列 / 交换机 / 绑定声明（含死信兜底）。
 */
@Configuration
public class RabbitMQConfig {

    // ---------------- 主交换机 / 队列 ----------------

    @Bean
    public DirectExchange escalationExchange() {
        return new DirectExchange(RabbitConstant.ESCALATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue escalationQueue() {
        return QueueBuilder.durable(RabbitConstant.ESCALATION_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitConstant.ESCALATION_DLX)
                .withArgument("x-dead-letter-routing-key", RabbitConstant.ESCALATION_DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding escalationBinding() {
        return BindingBuilder.bind(escalationQueue())
                .to(escalationExchange())
                .with(RabbitConstant.ESCALATION_ROUTING_KEY);
    }

    // ---------------- 死信交换机 / 队列 ----------------

    @Bean
    public DirectExchange escalationDlx() {
        return new DirectExchange(RabbitConstant.ESCALATION_DLX, true, false);
    }

    @Bean
    public Queue escalationDlq() {
        return QueueBuilder.durable(RabbitConstant.ESCALATION_DLQ).build();
    }

    @Bean
    public Binding escalationDlxBinding() {
        return BindingBuilder.bind(escalationDlq())
                .to(escalationDlx())
                .with(RabbitConstant.ESCALATION_DLX_ROUTING_KEY);
    }

    // ---------------- 消息转换器 ----------------

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
