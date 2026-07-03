package com.lumora.algorithms.common;

import com.lumora.model.AlgorithmType;
import com.lumora.model.DistanceMetric;
import com.lumora.model.SearchRequest;
import com.lumora.model.SearchResponse;
import com.lumora.model.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SearchArchitectureTest {

    @Autowired
    private SearchEngine searchEngine;

    @Autowired
    private SearchContext searchContext;

    private SearchRequest.SearchRequestBuilder validRequestBuilder;

    @BeforeEach
    void setUp() {
        validRequestBuilder = SearchRequest.builder()
                .query("What is HNSW?")
                .metric(DistanceMetric.COSINE)
                .topK(3)
                .workspaceId(1L);
    }

    @Test
    void testSearchEngineShouldAutowireSuccessfully() {
        assertNotNull(searchEngine);
        assertNotNull(searchContext);
    }

    @Test
    void testSearchStrategyThrowsUnsupportedOperationException() {
        SearchRequest request = validRequestBuilder.algorithm(AlgorithmType.HYBRID).build();

        // Template method should execute and invoke the subclass doSearch, throwing the expected error
        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () ->
                searchEngine.executeSearch(request));
        assertEquals("Not implemented yet", ex.getMessage());
    }

    @Test
    void testValidationConstraintValidationInTemplateMethod() {
        SearchRequest invalidRequest = SearchRequest.builder()
                .query("") // Invalid query (Blank)
                .algorithm(AlgorithmType.KD_TREE)
                .metric(DistanceMetric.EUCLIDEAN)
                .workspaceId(1L)
                .build();

        SearchException ex = assertThrows(SearchException.class, () ->
                searchEngine.executeSearch(invalidRequest));
        assertTrue(ex.getMessage().contains("validation failed"));
    }

    @Test
    void testSearchContextThrowsExceptionForMissingStrategy() {
        // Build a SearchContext manually with a missing strategy mapping
        Map<AlgorithmType, SearchStrategy> customMap = new EnumMap<>(AlgorithmType.class);
        SearchContext emptyContext = new SearchContext(customMap, null);

        SearchRequest request = validRequestBuilder.algorithm(AlgorithmType.BRUTE_FORCE).build();

        SearchException ex = assertThrows(SearchException.class, () ->
                emptyContext.search(request));
        assertTrue(ex.getMessage().contains("Unavailable search algorithm strategy"));
    }

    @Test
    void testTemplateMethodExecutionTiming() {
        // Create a dummy mock strategy to measure timing behavior
        SearchStrategy mockStrategy = new AbstractSearchStrategy() {
            @Override
            public AlgorithmType getAlgorithmType() {
                return AlgorithmType.BRUTE_FORCE;
            }

            @Override
            protected List<SearchResult> doSearch(SearchRequest request) {
                try {
                    Thread.sleep(50); // Simulate some work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return Collections.singletonList(SearchResult.builder().score(0.99).build());
            }
        };

        SearchRequest request = validRequestBuilder.algorithm(AlgorithmType.BRUTE_FORCE).build();
        SearchResponse response = mockStrategy.search(request);

        assertEquals(AlgorithmType.BRUTE_FORCE, response.getAlgorithm());
        assertEquals(1, response.getResultCount());
        // Verify execution time is recorded and > 0
        assertTrue(response.getExecutionTime() >= 45, "Execution time should be around 50ms (got: " + response.getExecutionTime() + "ms)");
    }
}
