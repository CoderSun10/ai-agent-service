package com.agent.service;

import com.agent.entity.BizLogistics;
import com.agent.mapper.BizLogisticsMapper;
import com.agent.vo.LogisticsVO;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 物流服务（数据真实存储于 MySQL biz_logistics 表）。
 */
@Service
@RequiredArgsConstructor
public class LogisticsService {

    private final BizLogisticsMapper logisticsMapper;

    /**
     * 根据订单号查询物流，未找到返回 null。
     */
    public LogisticsVO getByOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return null;
        }
        BizLogistics lo = logisticsMapper.selectOne(
                Wrappers.<BizLogistics>lambdaQuery().eq(BizLogistics::getOrderId, orderId.trim()));
        if (lo == null) {
            return null;
        }
        List<String> events;
        try {
            events = lo.getEventsJson() == null ? Collections.emptyList()
                    : JSON.parseArray(lo.getEventsJson(), String.class);
        } catch (Exception e) {
            events = Collections.emptyList();
        }
        return new LogisticsVO(
                lo.getOrderId(),
                lo.getCarrier(),
                lo.getTrackingNo(),
                lo.getCurrentStatus(),
                events);
    }
}
