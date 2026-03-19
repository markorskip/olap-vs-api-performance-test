package com.example.benchmark.grid;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GridProperties.class)
public class ClientConfig {

    @Bean
    RestClient customerRestClient(GridProperties properties) {
        return RestClient.builder().baseUrl(properties.customerBaseUrl()).build();
    }

    @Bean
    RestClient productRestClient(GridProperties properties) {
        return RestClient.builder().baseUrl(properties.productBaseUrl()).build();
    }
}
