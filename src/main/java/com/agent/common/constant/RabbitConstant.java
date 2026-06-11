package com.agent.common.constant;

/**
 * RabbitMQ 相关名称常量。
 */
public final class RabbitConstant {

    private RabbitConstant() {
    }

    /** 转人工交换机 */
    public static final String ESCALATION_EXCHANGE = "exchange.escalation";

    /** 转人工路由键 */
    public static final String ESCALATION_ROUTING_KEY = "key.agent";

    /** 转人工队列 */
    public static final String ESCALATION_QUEUE = "queue.escalation";

    /** 死信交换机 */
    public static final String ESCALATION_DLX = "exchange.escalation.dlx";

    /** 死信路由键 */
    public static final String ESCALATION_DLX_ROUTING_KEY = "key.agent.dlx";

    /** 死信队列 */
    public static final String ESCALATION_DLQ = "queue.escalation.dlq";
}
