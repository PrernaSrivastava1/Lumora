package com.lumora.analytics;

import com.lumora.model.AlgorithmType;
import com.lumora.model.DistanceMetric;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service that records search execution metrics in thread-safe, in-memory structures
 * and compiles latency averages and metrics aggregates.
 */
@Service
public class SearchAnalyticsService {

    private final List<SearchExecutionRecord> history = new CopyOnWriteArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Records a search execution run.
     */
    public void record(SearchExecutionRecord record) {
        if (record == null) {
            return;
        }
        record.setId(idGenerator.getAndIncrement());
        if (record.getTimestamp() == null) {
            record.setTimestamp(LocalDateTime.now());
        }
        history.add(record);
    }

    /**
     * Returns a copy of the recorded search history.
     */
    public List<SearchExecutionRecord> getHistory() {
        return new ArrayList<>(history);
    }

    /**
     * Computes the average latency of all successful search queries.
     *
     * @return average latency in milliseconds
     */
    public double getAverageLatency() {
        return history.stream()
                .filter(SearchExecutionRecord::isSuccess)
                .mapToLong(SearchExecutionRecord::getExecutionTimeMs)
                .average()
                .orElse(0.0);
    }

    /**
     * Computes the minimum latency of all successful queries.
     *
     * @return minimum latency in milliseconds
     */
    public long getMinLatency() {
        return history.stream()
                .filter(SearchExecutionRecord::isSuccess)
                .mapToLong(SearchExecutionRecord::getExecutionTimeMs)
                .min()
                .orElse(0L);
    }

    /**
     * Computes the maximum latency of all successful queries.
     *
     * @return maximum latency in milliseconds
     */
    public long getMaxLatency() {
        return history.stream()
                .filter(SearchExecutionRecord::isSuccess)
                .mapToLong(SearchExecutionRecord::getExecutionTimeMs)
                .max()
                .orElse(0L);
    }

    /**
     * Counts search executions grouped by algorithm type.
     */
    public Map<AlgorithmType, Long> getCountByAlgorithm() {
        Map<AlgorithmType, Long> counts = history.stream()
                .collect(Collectors.groupingBy(
                        SearchExecutionRecord::getAlgorithm,
                        () -> new EnumMap<>(AlgorithmType.class),
                        Collectors.counting()
                ));
        // Fill missing algorithms with zero counts
        for (AlgorithmType type : AlgorithmType.values()) {
            counts.putIfAbsent(type, 0L);
        }
        return counts;
    }

    /**
     * Counts search executions grouped by distance metric.
     */
    public Map<DistanceMetric, Long> getCountByMetric() {
        Map<DistanceMetric, Long> counts = history.stream()
                .collect(Collectors.groupingBy(
                        SearchExecutionRecord::getMetric,
                        () -> new EnumMap<>(DistanceMetric.class),
                        Collectors.counting()
                ));
        for (DistanceMetric metric : DistanceMetric.values()) {
            counts.putIfAbsent(metric, 0L);
        }
        return counts;
    }

    /**
     * Compiles a summary report of all search executions.
     */
    public Map<String, Object> getAnalyticsSummary() {
        long total = history.size();
        long successful = history.stream().filter(SearchExecutionRecord::isSuccess).count();
        double successRate = total == 0 ? 0.0 : (double) successful / total;

        return Map.of(
                "totalSearches", total,
                "successfulSearches", successful,
                "successRate", successRate,
                "averageLatencyMs", getAverageLatency(),
                "minLatencyMs", getMinLatency(),
                "maxLatencyMs", getMaxLatency(),
                "searchesByAlgorithm", getCountByAlgorithm(),
                "searchesByMetric", getCountByMetric()
        );
    }

    /**
     * Clears all metrics logs.
     */
    public void clearHistory() {
        history.clear();
        idGenerator.set(1);
        aiQuestionsCount.clear();
    }

    private final Map<Long, java.util.concurrent.atomic.AtomicLong> aiQuestionsCount = new java.util.concurrent.ConcurrentHashMap<>();

    public void incrementAiQuestionsCount(Long workspaceId) {
        if (workspaceId != null) {
            aiQuestionsCount.computeIfAbsent(workspaceId, k -> new java.util.concurrent.atomic.AtomicLong(0)).incrementAndGet();
        }
    }

    public long getAiQuestionsCount(Long workspaceId) {
        if (workspaceId == null) return 0;
        return aiQuestionsCount.computeIfAbsent(workspaceId, k -> new java.util.concurrent.atomic.AtomicLong(0)).get();
    }
}
