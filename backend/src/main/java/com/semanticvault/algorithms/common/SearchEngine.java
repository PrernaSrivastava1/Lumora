package com.semanticvault.algorithms.common;

import com.semanticvault.analytics.SearchAnalyticsService;
import com.semanticvault.analytics.SearchExecutionRecord;
import com.semanticvault.model.SearchRequest;
import com.semanticvault.model.SearchResponse;
import com.semanticvault.repository.VectorStore;
import org.springframework.stereotype.Service;

/**
 * Main search entry point for the application.
 * Exposes methods to query the underlying vector indexes by delegating to {@link SearchContext}.
 */
@Service
public class SearchEngine {

    private final SearchContext searchContext;
    private final SearchAnalyticsService analyticsService;
    private final VectorStore vectorStore;

    public SearchEngine(SearchContext searchContext,
                        SearchAnalyticsService analyticsService,
                        VectorStore vectorStore) {
        this.searchContext = searchContext;
        this.analyticsService = analyticsService;
        this.vectorStore = vectorStore;
    }

    /**
     * Entry point to execute similarity searches.
     * Captures telemetry for analytics monitoring.
     *
     * @param request the parameters of the search query
     * @return search query result list and metrics
     * @throws SearchException if validation or strategy execution fails
     */
    public SearchResponse executeSearch(SearchRequest request) {
        int totalVectors = vectorStore.count();
        long startTime = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = null;
        SearchResponse response = null;

        try {
            response = searchContext.search(request);
            success = true;
            return response;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            analyticsService.record(SearchExecutionRecord.builder()
                    .algorithm(request.getAlgorithm())
                    .metric(request.getMetric())
                    .topK(request.getTopK())
                    .totalVectors(totalVectors)
                    .executionTimeMs(duration)
                    .success(success)
                    .resultCount(response != null ? response.getResultCount() : 0)
                    .errorMessage(errorMessage)
                    .build());
        }
    }
}
