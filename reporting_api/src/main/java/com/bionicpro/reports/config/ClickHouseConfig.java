package com.bionicpro.reports.config;

import com.clickhouse.jdbc.ClickHouseDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
@Configuration
public class ClickHouseConfig {

    @Value("${spring.datasource.url}")
    private String dataSourceUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public DataSource clickHouseDataSource() {
        try {
            Properties properties = new Properties();
            properties.setProperty("user", username);
            properties.setProperty("password", password);
            properties.setProperty("socket_timeout", "30000");
            properties.setProperty("connect_timeout", "30000");
            properties.setProperty("connection_timeout", "30000");
            properties.setProperty("compress", "0");
            properties.setProperty("decompress", "0");

            log.info("Connecting to ClickHouse with URL: {}", dataSourceUrl);
            log.debug("ClickHouse connection properties: {}", properties);

            return new ClickHouseDataSource(dataSourceUrl, properties);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create ClickHouse DataSource", e);
        }
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setQueryTimeout(30);
        return jdbcTemplate;
    }
}