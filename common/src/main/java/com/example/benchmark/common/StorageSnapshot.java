package com.example.benchmark.common;

public record StorageSnapshot(
        long postgresUsedBytes,
        long clickHouseUsedBytes
) {
}
