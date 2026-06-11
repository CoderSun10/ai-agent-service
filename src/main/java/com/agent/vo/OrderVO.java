package com.agent.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单视图对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {

    private String orderId;
    /** 待付款 / 待发货 / 已发货 / 已签收 / 已退款 */
    private String status;
    private String productName;
    private BigDecimal amount;
    private String address;
    private String createTime;
}
