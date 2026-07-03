package com.lumora.analytics;

import com.lumora.model.AlgorithmType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object summarizing benchmark runs for a specific search algorithm.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenchmarkResult {
    private AlgorithmType algorithm;
    private double averageLatency;
    private long minimumLatency;
    private long maximumLatency;
    private int totalExecutions;
    private double successRate;
}
