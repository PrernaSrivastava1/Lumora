package com.lumora.analytics;

import com.lumora.dto.ApiResponse;
import com.lumora.model.DistanceMetric;
import com.lumora.model.SearchRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controller exposing REST APIs for search analytics and strategy benchmarking.
 */
@RestController
@RequestMapping
public class AnalyticsController {

    private final SearchAnalyticsService analyticsService;
    private final BenchmarkService benchmarkService;

    public AnalyticsController(SearchAnalyticsService analyticsService, BenchmarkService benchmarkService) {
        this.analyticsService = analyticsService;
        this.benchmarkService = benchmarkService;
    }

    /**
     * Retrieves the complete search execution record history.
     * GET /analytics/history
     */
    @GetMapping("/analytics/history")
    public ApiResponse<List<SearchExecutionRecord>> getHistory() {
        return ApiResponse.success("Retrieved search execution history", analyticsService.getHistory());
    }

    /**
     * Retrieves statistical summaries of search latencies and metrics counts.
     * GET /analytics/summary
     */
    @GetMapping("/analytics/summary")
    public ApiResponse<Map<String, Object>> getSummary() {
        return ApiResponse.success("Retrieved search analytics summary", analyticsService.getAnalyticsSummary());
    }

    /**
     * Clears all search execution analytics history records.
     * DELETE /analytics/clear
     */
    @DeleteMapping("/analytics/clear")
    public ApiResponse<Void> clearHistory() {
        analyticsService.clearHistory();
        return ApiResponse.success("Cleared analytics history log", null);
    }

    /**
     * Runs an on-demand benchmark comparison or retrieves historical summaries.
     * GET /benchmark
     */
    @GetMapping("/benchmark")
    public ApiResponse<List<BenchmarkResult>> runBenchmark(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "metric", defaultValue = "COSINE") DistanceMetric metric,
            @RequestParam(value = "k", defaultValue = "5") int k,
            @RequestParam(value = "workspaceId", defaultValue = "1") Long workspaceId) {

        if (query == null || query.trim().isEmpty()) {
            // Retrieve history summary
            return ApiResponse.success("Retrieved historical benchmark summary",
                    benchmarkService.generateBenchmarkSummary());
        }

        // Run on-demand benchmark comparing all strategy algorithms
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .metric(metric)
                .topK(k)
                .workspaceId(workspaceId)
                .build();

        List<BenchmarkResult> results = benchmarkService.runOnDemandBenchmark(request);
        return ApiResponse.success("Executed on-demand benchmark comparison", results);
    }
}
