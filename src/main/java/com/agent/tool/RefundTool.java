package com.agent.tool;

import com.agent.service.RefundService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 退款触发工具。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefundTool {

    private final RefundService refundService;

    @Tool("根据订单号发起退款申请，需要提供退款原因，返回退款单号")
    public String applyRefund(@P("订单号") String orderId, @P("退款原因") String reason) {
        log.info("[Tool] applyRefund orderId={} reason={}", orderId, reason);
        try {
            String refundId = refundService.apply(orderId, reason);
            return "退款申请已提交，退款单号：" + refundId + "，预计 1-3 个工作日原路退回。";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }
}
