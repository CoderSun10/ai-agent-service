package com.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AI 多 Agent 智能客服系统 启动类。
 *
 * @author 孙启鑫
 */
@SpringBootApplication
@EnableScheduling          // 启用 Outbox 定时投递
@EnableAsync               // 启用异步任务
@MapperScan("com.agent.mapper")
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
