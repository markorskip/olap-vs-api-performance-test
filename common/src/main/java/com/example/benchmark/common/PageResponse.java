package com.example.benchmark.common;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String sortBy,
        String sortDirection,
        String pattern,
        long durationMs,
        MemorySnapshot memory,
        StorageSnapshot storage
) {
}
