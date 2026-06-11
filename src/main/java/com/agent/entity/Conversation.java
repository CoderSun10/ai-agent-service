package com.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话。一个 conversation 对应一组连续对话。
 */
@Data
@TableName("conversation")
public class Conversation {

    /** UUID */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private Long userId;

    /** 0进行中 1已结束 2已转人工 */
    private Integer status;

    /** 是否触发低置信：0否 1是 */
    private Integer confidenceLow;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
