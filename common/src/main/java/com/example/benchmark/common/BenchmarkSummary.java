package com.example.benchmark.common;

import java.util.List;

public record BenchmarkSummary(
        String pattern,
        int iterations,
        long minDurationMs,
        long maxDurationMs,
        double averageDurationMs,
        MemorySnapshot memoryBefore,
        MemorySnapshot memoryAfter,
        List<BenchmarkIteration> samples
) {
}
