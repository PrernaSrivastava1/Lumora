package com.lumora.analytics;

import com.lumora.model.AlgorithmType;
import com.lumora.model.DistanceMetric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Capture record documenting parameters and latencies of a search query execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchExecutionRecord {
    private Long id;
    private LocalDateTime timestamp;
    private AlgorithmType algorithm;
    private DistanceMetric metric;
    private long executionTimeMs;
    private int topK;
    private int totalVectors;
    private int resultCount;
    private boolean success;
    private String errorMessage;
}
