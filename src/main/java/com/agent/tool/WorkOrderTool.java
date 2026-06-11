package com.agent.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工单创建工具。当问题需要人工跟进但尚未触发自动转人工时，由 Agent 主动创建工单。
 */
@Slf4j
@Component
public class WorkOrderTool {

    private final AtomicInteger seq = new AtomicInteger(0);

    @Tool("当用户问题需要人工介入处理时，创建一张人工工单，返回工单编号")
    public String createWorkOrder(@P("问题摘要") String summary, @P("紧急程度，如 低/中/高") String priority) {
        log.info("[Tool] createWorkOrder summary={} priority={}", summary, priority);
        String workOrderId = "W"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%04d", seq.incrementAndGet());
        return "已为您创建工单，工单编号：" + workOrderId
                + "（紧急程度：" + priority + "），客服人员会尽快与您联系。";
    }
}
