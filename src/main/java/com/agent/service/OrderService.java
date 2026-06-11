package com.agent.service;

import com.agent.entity.BizOrder;
import com.agent.mapper.BizOrderMapper;
import com.agent.vo.OrderVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * 订单服务（数据真实存储于 MySQL biz_order 表）。
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final BizOrderMapper orderMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 根据订单号查询订单，未找到返回 null。
     */
    public OrderVO getById(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return null;
        }
        BizOrder order = orderMapper.selectById(orderId.trim());
        if (order == null) {
            return null;
        }
        return new OrderVO(
                order.getOrderId(),
                order.getStatus(),
                order.getProductName(),
                order.getAmount(),
                order.getAddress(),
                order.getCreateTime() == null ? null : order.getCreateTime().format(FMT));
    }

    /**
     * 更新订单状态（退款时调用）。
     */
    public boolean updateStatus(String orderId, String status) {
        if (orderId == null) {
            return false;
        }
        BizOrder update = new BizOrder();
        update.setOrderId(orderId.trim());
        update.setStatus(status);
        return orderMapper.updateById(update) > 0;
    }

    /**
     * 订单是否存在。
     */
    public boolean exists(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return false;
        }
        return orderMapper.selectCount(
                Wrappers.<BizOrder>lambdaQuery().eq(BizOrder::getOrderId, orderId.trim())) > 0;
    }
}
