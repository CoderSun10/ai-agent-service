package com.agent.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * LangChain4j 与 pgvector 相关 Bean 配置。
 *
 * <p>ChatModel / StreamingChatModel / EmbeddingModel 均由
 * langchain4j-open-ai-spring-boot-starter 根据 application.yml 自动装配：
 * <ul>
 *   <li>chat-model / streaming-chat-model → DeepSeek（deepseek-chat）</li>
 *   <li>embedding-model → OpenAI（text-embedding-3-small，1536 维）</li>
 * </ul>
 * 本类只负责 pgvector 第二数据源与其 JdbcTemplate（手写余弦相似度检索）。
 */
@Configuration
public class LangChain4jConfig {

    @Value("${pgvector.url}")
    private String pgUrl;
    @Value("${pgvector.username}")
    private String pgUsername;
    @Value("${pgvector.password}")
    private String pgPassword;
    @Value("${pgvector.driver-class-name}")
    private String pgDriver;

    /**
     * pgvector 专用数据源（独立于 MySQL 主数据源）。
     */
    @Bean(name = "pgVectorDataSource")
    public DataSource pgVectorDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName(pgDriver)
                .url(pgUrl)
                .username(pgUsername)
                .password(pgPassword)
                .build();
    }

    /**
     * pgvector 专用 JdbcTemplate（手写余弦相似度 SQL 时使用）。
     */
    @Bean(name = "vectorJdbcTemplate")
    public JdbcTemplate vectorJdbcTemplate() {
        return new JdbcTemplate(pgVectorDataSource());
    }
}
