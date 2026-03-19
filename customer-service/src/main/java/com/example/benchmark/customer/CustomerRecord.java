package com.example.benchmark.customer;

import java.time.Instant;

public record CustomerRecord(
        long id,
        String externalId,
        String name,
        String region,
        String segment,
        String status,
        Instant createdAt
) {
}
