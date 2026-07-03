package com.lumora.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result wrapper representing complete search query responses and latency metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {

    private String query;

    /**
     * Latency of the query execution in microseconds or milliseconds.
     */
    private long executionTime;

    private AlgorithmType algorithm;

    private DistanceMetric metric;

    private int resultCount;

    private List<SearchResult> results;
}
