package com.example.benchmark.grid;

import com.example.benchmark.common.MemorySnapshot;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;

@Component
public class MemoryProbe {

    public MemorySnapshot sample() {
        var memoryMxBean = ManagementFactory.getMemoryMXBean();
        var heap = memoryMxBean.getHeapMemoryUsage();
        var nonHeap = memoryMxBean.getNonHeapMemoryUsage();
        return new MemorySnapshot(
                heap.getUsed(),
                heap.getCommitted(),
                nonHeap.getUsed(),
                System.getenv().getOrDefault("HOSTNAME", "local"),
                System.getenv().getOrDefault("POD_NAMESPACE", "local"));
    }
}
