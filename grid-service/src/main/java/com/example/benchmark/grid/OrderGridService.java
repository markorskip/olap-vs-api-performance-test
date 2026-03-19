package com.example.benchmark.grid;

import com.example.benchmark.common.BenchmarkIteration;
import com.example.benchmark.common.BenchmarkSummary;
import com.example.benchmark.common.MemorySnapshot;
import com.example.benchmark.common.OrderGridRow;
import com.example.benchmark.common.PageResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderGridService {
    private final OrderGridRepository repository;
    private final RestClient customerRestClient;
    private final RestClient productRestClient;
    private final MemoryProbe memoryProbe;

    public OrderGridService(OrderGridRepository repository,
                            @Qualifier("customerRestClient") RestClient customerRestClient,
                            @Qualifier("productRestClient") RestClient productRestClient,
                            MemoryProbe memoryProbe) {
        this.repository = repository;
        this.customerRestClient = customerRestClient;
        this.productRestClient = productRestClient;
        this.memoryProbe = memoryProbe;
    }

    public PageResponse<OrderGridRow> query(GridQueryRequest request) {
        Instant start = Instant.now();
        MemorySnapshot before = memoryProbe.sample();
        List<OrderGridRow> content;
        long total;
        switch (request.pattern()) {
            case A -> {
                List<OrderGridRow> allRows = queryInMemory(request.sortBy(), request.sortDirection());
                total = allRows.size();
                int fromIndex = Math.min(request.page() * request.size(), allRows.size());
                int toIndex = Math.min(fromIndex + request.size(), allRows.size());
                content = allRows.subList(fromIndex, toIndex);
            }
            case B -> {
                total = repository.countViewRows();
                content = repository.queryViewPage(request.sortBy(), request.sortDirection(), request.size(), request.page() * request.size());
            }
            case C -> {
                total = repository.countClickHouseRows();
                content = repository.queryClickHousePage(request.sortBy(), request.sortDirection(), request.size(), request.page() * request.size());
            }
            default -> throw new IllegalStateException("Unexpected value: " + request.pattern());
        }
        long duration = Duration.between(start, Instant.now()).toMillis();
        return new PageResponse<>(
                content,
                request.page(),
                request.size(),
                total,
                (int) Math.ceil((double) total / request.size()),
                request.sortBy(),
                request.sortDirection(),
                request.pattern().name(),
                duration,
                before);
    }

    public BenchmarkSummary benchmark(BenchmarkRequest request) {
        MemorySnapshot before = memoryProbe.sample();
        List<BenchmarkIteration> iterations = new ArrayList<>();
        for (int i = 1; i <= request.iterations(); i++) {
            PageResponse<OrderGridRow> response = query(new GridQueryRequest(
                    request.pattern(),
                    request.page(),
                    request.size(),
                    request.sortBy(),
                    request.sortDirection()));
            iterations.add(new BenchmarkIteration(i, response.durationMs(), memoryProbe.sample()));
        }
        long min = iterations.stream().mapToLong(BenchmarkIteration::durationMs).min().orElse(0);
        long max = iterations.stream().mapToLong(BenchmarkIteration::durationMs).max().orElse(0);
        double avg = iterations.stream().mapToLong(BenchmarkIteration::durationMs).average().orElse(0);
        return new BenchmarkSummary(request.pattern().name(), request.iterations(), min, max, avg, before, memoryProbe.sample(), iterations);
    }

    private List<OrderGridRow> queryInMemory(String sortBy, String sortDirection) {
        List<OrderRecord> orders = repository.findAllOrders();
        List<Long> customerIds = orders.stream().map(OrderRecord::customerId).distinct().toList();
        List<Long> productIds = orders.stream().map(OrderRecord::productId).distinct().toList();

        Map<Long, CustomerRecord> customers = customerRestClient.post()
                .uri("/api/customers/batch")
                .body(customerIds)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CustomerRecord>>() {})
                .stream()
                .collect(Collectors.toMap(CustomerRecord::id, Function.identity()));

        Map<Long, ProductRecord> products = productRestClient.post()
                .uri("/api/products/batch")
                .body(productIds)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProductRecord>>() {})
                .stream()
                .collect(Collectors.toMap(ProductRecord::id, Function.identity()));

        Comparator<OrderGridRow> comparator = comparator(sortBy, sortDirection);
        return orders.stream()
                .map(order -> join(order, customers, products))
                .sorted(comparator)
                .toList();
    }

    private Comparator<OrderGridRow> comparator(String sortBy, String sortDirection) {
        Map<String, Comparator<OrderGridRow>> comparators = new HashMap<>();
        comparators.put("orderId", Comparator.comparingLong(OrderGridRow::orderId));
        comparators.put("customerId", Comparator.comparingLong(OrderGridRow::customerId));
        comparators.put("customerName", Comparator.comparing(OrderGridRow::customerName, Comparator.nullsLast(String::compareToIgnoreCase)));
        comparators.put("customerRegion", Comparator.comparing(OrderGridRow::customerRegion, Comparator.nullsLast(String::compareToIgnoreCase)));
        comparators.put("customerSegment", Comparator.comparing(OrderGridRow::customerSegment, Comparator.nullsLast(String::compareToIgnoreCase)));
        comparators.put("productId", Comparator.comparingLong(OrderGridRow::productId));
        comparators.put("productSku", Comparator.comparing(OrderGridRow::productSku, Comparator.nullsLast(String::compareToIgnoreCase)));
        comparators.put("productName", Comparator.comparing(OrderGridRow::productName, Comparator.nullsLast(String::compareToIgnoreCase)));
        comparators.put("productCategory", Comparator.comparing(OrderGridRow::productCategory, Comparator.nullsLast(String::compareToIgnoreCase)));
        comparators.put("quantity", Comparator.comparingInt(OrderGridRow::quantity));
        comparators.put("unitPrice", Comparator.comparing(OrderGridRow::unitPrice));
        comparators.put("extendedPrice", Comparator.comparing(OrderGridRow::extendedPrice));
        comparators.put("orderStatus", Comparator.comparing(OrderGridRow::orderStatus, Comparator.nullsLast(String::compareToIgnoreCase)));
        comparators.put("salesChannel", Comparator.comparing(OrderGridRow::salesChannel, Comparator.nullsLast(String::compareToIgnoreCase)));
        comparators.put("orderedAt", Comparator.comparing(OrderGridRow::orderedAt));
        Comparator<OrderGridRow> comparator = comparators.get(sortBy);
        if (comparator == null) {
            throw new IllegalArgumentException("Unsupported sort column: " + sortBy);
        }
        comparator = comparator.thenComparingLong(OrderGridRow::orderId);
        return "desc".equalsIgnoreCase(sortDirection) ? comparator.reversed() : comparator;
    }

    private OrderGridRow join(OrderRecord order, Map<Long, CustomerRecord> customers, Map<Long, ProductRecord> products) {
        CustomerRecord customer = customers.get(order.customerId());
        ProductRecord product = products.get(order.productId());
        BigDecimal extendedPrice = order.unitPrice().multiply(BigDecimal.valueOf(order.quantity()));
        return new OrderGridRow(
                order.id(),
                order.customerId(),
                customer != null ? customer.name() : "UNKNOWN",
                customer != null ? customer.region() : "UNKNOWN",
                customer != null ? customer.segment() : "UNKNOWN",
                order.productId(),
                product != null ? product.sku() : "UNKNOWN",
                product != null ? product.name() : "UNKNOWN",
                product != null ? product.category() : "UNKNOWN",
                order.quantity(),
                order.unitPrice(),
                extendedPrice,
                order.orderStatus(),
                order.salesChannel(),
                order.orderedAt());
    }
}
