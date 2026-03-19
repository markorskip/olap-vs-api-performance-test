package com.example.benchmark.grid;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource")
    DataSourceProperties postgresProperties() {
        return new DataSourceProperties();
    }

    @Bean
    DataSource postgresDataSource(@Qualifier("postgresProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    JdbcClient postgresJdbcClient(@Qualifier("postgresDataSource") DataSource dataSource) {
        return JdbcClient.create(dataSource);
    }

    @Bean
    @ConfigurationProperties("app.clickhouse")
    DataSourceProperties clickHouseProperties() {
        return new DataSourceProperties();
    }

    @Bean
    DataSource clickHouseDataSource(@Qualifier("clickHouseProperties") DataSourceProperties properties) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(properties.getDriverClassName());
        dataSource.setUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        return dataSource;
    }

    @Bean
    JdbcClient clickHouseJdbcClient(@Qualifier("clickHouseDataSource") DataSource dataSource) {
        return JdbcClient.create(dataSource);
    }
}
