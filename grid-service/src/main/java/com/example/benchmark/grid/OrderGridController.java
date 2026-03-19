package com.example.benchmark.grid;

import com.example.benchmark.common.BenchmarkSummary;
import com.example.benchmark.common.OrderGridRow;
import com.example.benchmark.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grid/orders")
public class OrderGridController {
    private final OrderGridService service;

    public OrderGridController(OrderGridService service) {
        this.service = service;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<OrderGridRow> getPage(
            @RequestParam(defaultValue = "A") QueryPattern pattern,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "orderedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return service.query(new GridQueryRequest(pattern, page, size, sortBy, sortDirection));
    }

    @PostMapping("/benchmark")
    @ResponseStatus(HttpStatus.OK)
    public BenchmarkSummary benchmark(@Valid @RequestBody BenchmarkRequest request) {
        return service.benchmark(request);
    }
}
