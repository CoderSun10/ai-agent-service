package com.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Outbox 本地消息表。配合定时任务保证 RabbitMQ 消息不丢失。
 */
@Data
@TableName("outbox_message")
public class OutboxMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String conversationId;

    /** JSON 字符串 payload */
    private String payload;

    /** 0待发送 1已发送 */
    private Integer status;

    private Integer retryCount;

    private LocalDateTime createTime;
}
