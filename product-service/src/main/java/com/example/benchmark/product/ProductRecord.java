package com.example.benchmark.product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductRecord(
        long id,
        String sku,
        String name,
        String category,
        String status,
        BigDecimal listPrice,
        Instant createdAt
) {
}
