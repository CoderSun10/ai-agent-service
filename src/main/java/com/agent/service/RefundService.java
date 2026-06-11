package com.agent.service;

import com.agent.entity.RefundRecord;
import com.agent.mapper.RefundRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 退款服务（退款记录真实落库 MySQL refund_record 表，并更新订单状态）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final OrderService orderService;
    private final RefundRecordMapper refundMapper;

    /**
     * 发起退款：校验订单 → 更新订单状态为"退款中" → 写退款记录。返回退款单号。
     *
     * @param orderId 订单号
     * @param reason  退款原因
     * @return 退款单号
     */
    @Transactional
    public String apply(String orderId, String reason) {
        if (!orderService.exists(orderId)) {
            throw new IllegalArgumentException("订单不存在，无法退款：" + orderId);
        }
        orderService.updateStatus(orderId, "退款中");

        String refundId = "R"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%05d", ThreadLocalRandom.current().nextInt(100000));

        RefundRecord record = new RefundRecord();
        record.setRefundId(refundId);
        record.setOrderId(orderId.trim());
        record.setReason(reason);
        record.setStatus("处理中");
        refundMapper.insert(record);

        log.info("发起退款 orderId={} reason={} refundId={}", orderId, reason, refundId);
        return refundId;
    }
}
