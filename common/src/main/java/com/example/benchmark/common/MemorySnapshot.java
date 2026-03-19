package com.example.benchmark.common;

public record MemorySnapshot(
        long heapUsedBytes,
        long heapCommittedBytes,
        long nonHeapUsedBytes,
        String podName,
        String namespace
) {
}
