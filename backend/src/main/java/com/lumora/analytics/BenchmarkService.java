package com.lumora.analytics;

import com.lumora.algorithms.common.SearchStrategy;
import com.lumora.model.AlgorithmType;
import com.lumora.model.SearchRequest;
import com.lumora.model.SearchResponse;
import com.lumora.model.SearchResult;
import com.lumora.repository.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service orchestrating query benchmarking and comparison reports across search strategies.
 */
@Service
public class BenchmarkService {

    private final SearchAnalyticsService analyticsService;
    private final List<SearchStrategy> strategies;
    private final VectorStore vectorStore;

    public BenchmarkService(SearchAnalyticsService analyticsService,
                            List<SearchStrategy> strategies,
                            VectorStore vectorStore) {
        this.analyticsService = analyticsService;
        this.strategies = strategies;
        this.vectorStore = vectorStore;
    }

    /**
     * Summarizes historical runs into a list of algorithm performance benchmarks.
     *
     * @return performance summaries grouped by algorithm
     */
    public List<BenchmarkResult> generateBenchmarkSummary() {
        List<SearchExecutionRecord> history = analyticsService.getHistory();

        Map<AlgorithmType, List<SearchExecutionRecord>> grouped = history.stream()
                .collect(Collectors.groupingBy(SearchExecutionRecord::getAlgorithm));

        List<BenchmarkResult> results = new ArrayList<>();
        grouped.forEach((algorithm, records) -> {
            int total = records.size();
            long successful = records.stream().filter(SearchExecutionRecord::isSuccess).count();
            double successRate = total == 0 ? 0.0 : (double) successful / total;

            double avgLatency = records.stream()
                    .filter(SearchExecutionRecord::isSuccess)
                    .mapToLong(SearchExecutionRecord::getExecutionTimeMs)
                    .average()
                    .orElse(0.0);

            long minLatency = records.stream()
                    .filter(SearchExecutionRecord::isSuccess)
                    .mapToLong(SearchExecutionRecord::getExecutionTimeMs)
                    .min()
                    .orElse(0L);

            long maxLatency = records.stream()
                    .filter(SearchExecutionRecord::isSuccess)
                    .mapToLong(SearchExecutionRecord::getExecutionTimeMs)
                    .max()
                    .orElse(0L);

            double avgAccuracy = records.stream()
                    .filter(SearchExecutionRecord::isSuccess)
                    .mapToDouble(SearchExecutionRecord::getAccuracy)
                    .average()
                    .orElse(1.0);

            results.add(BenchmarkResult.builder()
                    .algorithm(algorithm)
                    .totalExecutions(total)
                    .successRate(successRate)
                    .averageLatency(avgLatency)
                    .minimumLatency(minLatency)
                    .maximumLatency(maxLatency)
                    .accuracy(avgAccuracy)
                    .build());
        });

        return results;
    }

    /**
     * Executes all available search strategies using the same request parameters,
     * recording results and returning a comparative benchmark performance list.
     * Captures and logs exceptions if a strategy throws an implementation error.
     *
     * @param request search parameters
     * @return dynamic benchmarking comparison
     */
    public List<BenchmarkResult> runOnDemandBenchmark(SearchRequest request) {
        int totalVectors = vectorStore.count(request.getWorkspaceId());

        java.util.Set<Long> baselineIds = new java.util.HashSet<>();
        // Find brute force strategy and run it first to get baseline
        for (SearchStrategy strategy : strategies) {
            if (strategy.getAlgorithmType() == AlgorithmType.BRUTE_FORCE) {
                try {
                    SearchRequest customRequest = SearchRequest.builder()
                            .query(request.getQuery())
                            .algorithm(AlgorithmType.BRUTE_FORCE)
                            .metric(request.getMetric())
                            .topK(request.getTopK())
                            .workspaceId(request.getWorkspaceId())
                            .build();
                    SearchResponse response = strategy.search(customRequest);
                    if (response != null && response.getResults() != null) {
                        for (SearchResult r : response.getResults()) {
                            if (r.getChunkId() != null) {
                                baselineIds.add(r.getChunkId());
                            }
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
                break;
            }
        }

        for (SearchStrategy strategy : strategies) {
            long start = System.currentTimeMillis();
            boolean success = false;
            String errorMsg = null;
            int resultCount = 0;
            double accuracy = 1.0;

            try {
                SearchRequest customRequest = SearchRequest.builder()
                        .query(request.getQuery())
                        .algorithm(strategy.getAlgorithmType())
                        .metric(request.getMetric())
                        .topK(request.getTopK())
                        .workspaceId(request.getWorkspaceId())
                        .build();

                SearchResponse response = strategy.search(customRequest);
                resultCount = response.getResultCount();
                success = true;

                if (strategy.getAlgorithmType() == AlgorithmType.BRUTE_FORCE) {
                    accuracy = 1.0;
                } else if (response != null && response.getResults() != null && !baselineIds.isEmpty()) {
                    long overlap = response.getResults().stream()
                            .map(SearchResult::getChunkId)
                            .filter(baselineIds::contains)
                            .count();
                    accuracy = (double) overlap / Math.min(request.getTopK(), baselineIds.size());
                }
            } catch (Exception e) {
                errorMsg = e.getMessage();
            }

            long elapsed = System.currentTimeMillis() - start;

            analyticsService.record(SearchExecutionRecord.builder()
                    .workspaceId(request.getWorkspaceId())
                    .algorithm(strategy.getAlgorithmType())
                    .metric(request.getMetric())
                    .topK(request.getTopK())
                    .totalVectors(totalVectors)
                    .executionTimeMs(elapsed)
                    .success(success)
                    .resultCount(resultCount)
                    .errorMessage(errorMsg)
                    .accuracy(accuracy)
                    .build());
        }

        // Return comparative output summary
        return generateBenchmarkSummary();
    }
}
