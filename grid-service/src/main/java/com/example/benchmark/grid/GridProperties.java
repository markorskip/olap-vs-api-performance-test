package com.example.benchmark.grid;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.services")
public record GridProperties(String customerBaseUrl, String productBaseUrl) {
}
