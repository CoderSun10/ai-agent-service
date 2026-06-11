package com.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息。
 */
@Data
@TableName("message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String conversationId;

    /** user / assistant / tool */
    private String role;

    private String content;

    /** 若为工具调用结果则记录工具名 */
    private String toolName;

    private LocalDateTime createTime;
}
