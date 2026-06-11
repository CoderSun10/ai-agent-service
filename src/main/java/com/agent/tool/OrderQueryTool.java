package com.agent.tool;

import com.agent.service.OrderService;
import com.agent.vo.OrderVO;
import com.alibaba.fastjson2.JSON;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单查询工具。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderQueryTool {

    private final OrderService orderService;

    @Tool("根据订单号查询订单详情，包括订单状态、商品名称、金额和收货地址")
    public String queryOrder(@P("订单号") String orderId) {
        log.info("[Tool] queryOrder orderId={}", orderId);
        OrderVO order = orderService.getById(orderId);
        if (order == null) {
            return "未找到订单：" + orderId + "，请核对订单号是否正确。";
        }
        return JSON.toJSONString(order);
    }
}
