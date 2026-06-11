package com.agent.tool;

import com.agent.service.LogisticsService;
import com.agent.vo.LogisticsVO;
import com.alibaba.fastjson2.JSON;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 物流追踪工具。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogisticsTool {

    private final LogisticsService logisticsService;

    @Tool("根据订单号查询物流信息，包括承运商、运单号、当前状态和物流轨迹")
    public String queryLogistics(@P("订单号") String orderId) {
        log.info("[Tool] queryLogistics orderId={}", orderId);
        LogisticsVO logistics = logisticsService.getByOrderId(orderId);
        if (logistics == null) {
            return "未查询到订单 " + orderId + " 的物流信息，可能尚未发货。";
        }
        return JSON.toJSONString(logistics);
    }
}
