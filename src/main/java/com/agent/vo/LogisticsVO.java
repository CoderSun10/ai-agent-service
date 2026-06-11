package com.agent.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 物流视图对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsVO {

    private String orderId;
    private String carrier;
    private String trackingNo;
    private String currentStatus;
    /** 物流轨迹（时间 + 描述） */
    private List<String> events;
}
