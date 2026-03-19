package com.example.benchmark.grid;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GridQueryRequest(
        @NotNull QueryPattern pattern,
        @Min(0) int page,
        @Min(1) @Max(100) int size,
        @NotBlank String sortBy,
        @NotBlank String sortDirection
) {
}
