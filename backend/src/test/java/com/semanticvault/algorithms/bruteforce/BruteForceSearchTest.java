package com.semanticvault.algorithms.bruteforce;

import com.semanticvault.algorithms.common.SearchException;
import com.semanticvault.algorithms.common.vector.Vector;
import com.semanticvault.model.AlgorithmType;
import com.semanticvault.model.DistanceMetric;
import com.semanticvault.model.SearchRequest;
import com.semanticvault.model.SearchResponse;
import com.semanticvault.model.SearchResult;
import com.semanticvault.repository.VectorStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BruteForceSearchTest {

    @Autowired
    private BruteForceSearchStrategy strategy;

    @Autowired
    private VectorStore vectorStore;

    @BeforeEach
    void setUp() {
        vectorStore.clear();
    }

    @Test
    void testSearchOnEmptyStoreReturnsEmptyList() {
        SearchRequest request = SearchRequest.builder()
                .query("0.1, 0.2, 0.3")
                .algorithm(AlgorithmType.BRUTE_FORCE)
                .metric(DistanceMetric.EUCLIDEAN)
                .topK(2)
                .workspaceId(1L)
                .build();

        SearchResponse response = strategy.search(request);
        assertEquals(0, response.getResultCount());
        assertTrue(response.getResults().isEmpty());
    }

    @Test
    void testSearchOnSingleVectorStore() {
        vectorStore.add(new Vector(10L, new float[]{0.1f, 0.2f, 0.3f}, 100L));

        SearchRequest request = SearchRequest.builder()
                .query("0.1, 0.2, 0.3")
                .algorithm(AlgorithmType.BRUTE_FORCE)
                .metric(DistanceMetric.EUCLIDEAN)
                .topK(1)
                .workspaceId(1L)
                .build();

        SearchResponse response = strategy.search(request);
        assertEquals(1, response.getResultCount());
        SearchResult topHit = response.getResults().getFirst();
        assertEquals(10L, topHit.getChunkId());
        assertEquals(0.0, topHit.getScore(), 1e-9);
    }

    @Test
    void testSearchOnMultipleVectorsWithVaryingDistances() {
        // Target: [1.0, 0.0]
        // Candidates:
        // V1: [1.0, 0.0] (Exact match)
        // V2: [0.0, 1.0] (Orthogonal)
        // V3: [0.8, 0.6] (Closer to V1 than V2)
        vectorStore.add(new Vector(1L, new float[]{1.0f, 0.0f}, 100L));
        vectorStore.add(new Vector(2L, new float[]{0.0f, 1.0f}, 101L));
        vectorStore.add(new Vector(3L, new float[]{0.8f, 0.6f}, 102L));

        SearchRequest request = SearchRequest.builder()
                .query("1.0, 0.0")
                .algorithm(AlgorithmType.BRUTE_FORCE)
                .metric(DistanceMetric.EUCLIDEAN)
                .topK(3)
                .workspaceId(1L)
                .build();

        SearchResponse response = strategy.search(request);
        assertEquals(3, response.getResultCount());

        List<SearchResult> results = response.getResults();

        // Exact match should be first
        assertEquals(1L, results.get(0).getChunkId());
        assertEquals(0.0, results.get(0).getScore(), 1e-9);

        // V3 is closer than V2: Euclidean of V3 is sqrt((1-0.8)^2 + (0-0.6)^2) = sqrt(0.04+0.36) = sqrt(0.4) = ~0.63
        // Euclidean of V2 is sqrt(2) = ~1.41
        assertEquals(3L, results.get(1).getChunkId());
        assertEquals(2L, results.get(2).getChunkId());
    }

    @Test
    void testSearchMetricCosineDistanceCalculations() {
        vectorStore.add(new Vector(1L, new float[]{1.0f, 0.0f}, 100L));
        vectorStore.add(new Vector(2L, new float[]{0.0f, 1.0f}, 101L));

        SearchRequest request = SearchRequest.builder()
                .query("1.0, 0.0")
                .algorithm(AlgorithmType.BRUTE_FORCE)
                .metric(DistanceMetric.COSINE)
                .topK(2)
                .workspaceId(1L)
                .build();

        SearchResponse response = strategy.search(request);
        List<SearchResult> results = response.getResults();

        assertEquals(1L, results.get(0).getChunkId());
        assertEquals(0.0, results.get(0).getScore(), 1e-9); // Identical

        assertEquals(2L, results.get(1).getChunkId());
        assertEquals(1.0, results.get(1).getScore(), 1e-9); // Orthogonal has distance 1.0
    }

    @Test
    void testInvalidSearchRequestsThrowException() {
        vectorStore.add(new Vector(1L, new float[]{1.0f, 2.0f}, 100L));

        // Invalid K (<= 0)
        SearchRequest reqBadK = SearchRequest.builder()
                .query("1.0, 2.0")
                .algorithm(AlgorithmType.BRUTE_FORCE)
                .metric(DistanceMetric.EUCLIDEAN)
                .topK(0)
                .workspaceId(1L)
                .build();
        assertThrows(SearchException.class, () -> strategy.search(reqBadK));

        // Query Dimension mismatch (Expected 2, Query is 3)
        SearchRequest reqBadDim = SearchRequest.builder()
                .query("1.0, 2.0, 3.0")
                .algorithm(AlgorithmType.BRUTE_FORCE)
                .metric(DistanceMetric.EUCLIDEAN)
                .topK(2)
                .workspaceId(1L)
                .build();
        assertThrows(SearchException.class, () -> strategy.search(reqBadDim));

        // Null/empty query
        SearchRequest reqNullQuery = SearchRequest.builder()
                .query("")
                .algorithm(AlgorithmType.BRUTE_FORCE)
                .metric(DistanceMetric.EUCLIDEAN)
                .topK(2)
                .workspaceId(1L)
                .build();
        assertThrows(SearchException.class, () -> strategy.search(reqNullQuery));
    }
}
