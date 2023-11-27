package com.xazktx.flowable.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源配置
 */
@Configuration
public class DataSourceConfig {

    /**
     * flowable
     * @return
     */
    @Primary
    @Bean(name = "flowableDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.flowable")
    public DataSource flowableDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * KFQGGK
     * @return
     */
    @Bean(name = "kfqggkDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.kfqggk")
    public DataSource kfqggkDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * KFQSXK
     * @return
     */
    @Bean(name = "kfqsxkDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.kfqsxk")
    public DataSource kfqsxkDataSource() {
        return DataSourceBuilder.create().build();
    }

}
