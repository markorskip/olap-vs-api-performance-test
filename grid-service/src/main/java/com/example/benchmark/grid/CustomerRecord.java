package com.example.benchmark.grid;

public record CustomerRecord(
        long id,
        String externalId,
        String name,
        String region,
        String segment,
        String status,
        String createdAt
) {
}
