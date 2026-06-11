package com.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 退款记录（业务数据，真实存储于 MySQL）。
 */
@Data
@TableName("refund_record")
public class RefundRecord {

    /** 退款单号，业务主键 */
    @TableId(type = IdType.INPUT)
    private String refundId;

    private String orderId;

    private String reason;

    /** 处理中/已退款/已驳回 */
    private String status;

    private LocalDateTime createTime;
}
