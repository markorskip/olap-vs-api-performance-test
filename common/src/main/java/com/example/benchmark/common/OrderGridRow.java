package com.example.benchmark.common;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderGridRow(
        long orderId,
        long customerId,
        String customerName,
        String customerRegion,
        String customerSegment,
        long productId,
        String productSku,
        String productName,
        String productCategory,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal extendedPrice,
        String orderStatus,
        String salesChannel,
        Instant orderedAt
) {
}
