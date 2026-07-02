package com.semanticvault.analytics;

import com.semanticvault.algorithms.common.vector.Vector;
import com.semanticvault.model.AlgorithmType;
import com.semanticvault.model.DistanceMetric;
import com.semanticvault.repository.VectorStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SearchAnalyticsAndBenchmarkTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SearchAnalyticsService analyticsService;

    @Autowired
    private BenchmarkService benchmarkService;

    @Autowired
    private VectorStore vectorStore;

    @BeforeEach
    void setUp() {
        analyticsService.clearHistory();
        vectorStore.clear();
    }

    @Test
    void testAnalyticsServiceCalculations() {
        analyticsService.record(SearchExecutionRecord.builder()
                .algorithm(AlgorithmType.BRUTE_FORCE)
                .metric(DistanceMetric.EUCLIDEAN)
                .executionTimeMs(10L)
                .success(true)
                .build());

        analyticsService.record(SearchExecutionRecord.builder()
                .algorithm(AlgorithmType.BRUTE_FORCE)
                .metric(DistanceMetric.COSINE)
                .executionTimeMs(20L)
                .success(true)
                .build());

        // Failed search should not be computed for latency metrics
        analyticsService.record(SearchExecutionRecord.builder()
                .algorithm(AlgorithmType.HNSW)
                .metric(DistanceMetric.COSINE)
                .executionTimeMs(50L)
                .success(false)
                .build());

        assertEquals(15.0, analyticsService.getAverageLatency(), 1e-9);
        assertEquals(10L, analyticsService.getMinLatency());
        assertEquals(20L, analyticsService.getMaxLatency());

        assertEquals(2L, analyticsService.getCountByAlgorithm().get(AlgorithmType.BRUTE_FORCE));
        assertEquals(1L, analyticsService.getCountByAlgorithm().get(AlgorithmType.HNSW));
    }

    @Test
    void testBenchmarkReportSummarizesHistoryCorrectly() {
        analyticsService.record(SearchExecutionRecord.builder()
                .algorithm(AlgorithmType.BRUTE_FORCE)
                .metric(DistanceMetric.EUCLIDEAN)
                .executionTimeMs(10L)
                .success(true)
                .build());

        List<BenchmarkResult> report = benchmarkService.generateBenchmarkSummary();
        assertEquals(1, report.size());
        BenchmarkResult bfResult = report.getFirst();
        assertEquals(AlgorithmType.BRUTE_FORCE, bfResult.getAlgorithm());
        assertEquals(10.0, bfResult.getAverageLatency());
        assertEquals(1.0, bfResult.getSuccessRate());
    }

    @Test
    void testControllerEndpointsHistoryAndSummary() throws Exception {
        analyticsService.record(SearchExecutionRecord.builder()
                .algorithm(AlgorithmType.BRUTE_FORCE)
                .metric(DistanceMetric.EUCLIDEAN)
                .executionTimeMs(5L)
                .success(true)
                .build());

        mockMvc.perform(get("/analytics/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].algorithm", is("BRUTE_FORCE")));

        mockMvc.perform(get("/analytics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalSearches", is(1)))
                .andExpect(jsonPath("$.data.averageLatencyMs", is(5.0)));
    }

    @Test
    void testClearAnalyticsHistoryEndpoint() throws Exception {
        analyticsService.record(SearchExecutionRecord.builder()
                .algorithm(AlgorithmType.BRUTE_FORCE)
                .success(true)
                .build());

        mockMvc.perform(delete("/analytics/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        assertTrue(analyticsService.getHistory().isEmpty());
    }

    @Test
    void testOnDemandBenchmarkEndpoint() throws Exception {
        // Populating one vector to prevent dimension mismatch check crashes
        vectorStore.add(new Vector(1L, new float[]{0.1f, 0.2f}, 100L));

        mockMvc.perform(get("/benchmark")
                        .param("q", "0.1, 0.2")
                        .param("metric", "EUCLIDEAN")
                        .param("k", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(4))); // 4 strategies (BF, HNSW, KD, Hybrid)
    }
}
