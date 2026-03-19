package com.example.benchmark.common;

public record BenchmarkIteration(int iteration, long durationMs, MemorySnapshot memorySnapshot) {
}
