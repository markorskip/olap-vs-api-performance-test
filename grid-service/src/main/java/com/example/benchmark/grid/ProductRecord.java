package com.example.benchmark.grid;

import java.math.BigDecimal;

public record ProductRecord(
        long id,
        String sku,
        String name,
        String category,
        String status,
        BigDecimal listPrice,
        String createdAt
) {
}
