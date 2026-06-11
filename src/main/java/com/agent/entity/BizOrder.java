package com.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单（业务数据，真实存储于 MySQL）。
 */
@Data
@TableName("biz_order")
public class BizOrder {

    /** 订单号，业务主键（非自增） */
    @TableId(type = IdType.INPUT)
    private String orderId;

    private String productName;

    /** 待付款/待发货/已发货/已签收/退款中/已退款 */
    private String status;

    private BigDecimal amount;

    private String address;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
