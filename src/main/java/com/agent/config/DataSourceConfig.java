package com.agent.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 主数据源（MySQL）显式声明。
 *
 * <p>因为本项目额外定义了 pgvector 第二数据源（{@link LangChain4jConfig#pgVectorDataSource()}），
 * Spring Boot 的 DataSource 自动配置会因「已存在 DataSource Bean」而退避，
 * 导致 MyBatis-Plus 误用 pg 数据源。这里显式把 MySQL 标记为 {@code @Primary} 主数据源。
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties mainDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource(DataSourceProperties mainDataSourceProperties) {
        return mainDataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
}
