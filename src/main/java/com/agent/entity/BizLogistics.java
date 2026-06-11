package com.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流（业务数据，真实存储于 MySQL）。
 */
@Data
@TableName("biz_logistics")
public class BizLogistics {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderId;

    private String carrier;

    private String trackingNo;

    private String currentStatus;

    /** 物流轨迹，JSON 数组字符串 */
    private String eventsJson;

    private LocalDateTime createTime;
}
