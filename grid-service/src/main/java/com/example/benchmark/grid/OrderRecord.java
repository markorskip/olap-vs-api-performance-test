package com.example.benchmark.grid;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderRecord(
        long id,
        long customerId,
        long productId,
        int quantity,
        BigDecimal unitPrice,
        String orderStatus,
        String salesChannel,
        Instant orderedAt
) {
}
