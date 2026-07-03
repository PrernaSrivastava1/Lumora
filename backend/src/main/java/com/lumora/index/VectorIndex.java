package com.lumora.index;

import com.lumora.algorithms.common.vector.Vector;
import com.lumora.model.IndexStats;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory vector index for a single workspace.
 */
public class VectorIndex {

    private final Long workspaceId;
    private final Map<Long, Vector> vectors = new ConcurrentHashMap<>();
    private volatile LocalDateTime lastUpdatedAt = LocalDateTime.now();

    public VectorIndex(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void add(Vector vector) {
        if (vector == null || vector.getId() == null) {
            return;
        }
        vectors.put(vector.getId(), vector);
        lastUpdatedAt = LocalDateTime.now();
    }

    public boolean remove(Long id) {
        if (id == null) {
            return false;
        }
        boolean removed = vectors.remove(id) != null;
        if (removed) {
            lastUpdatedAt = LocalDateTime.now();
        }
        return removed;
    }

    public Vector get(Long id) {
        if (id == null) {
            return null;
        }
        return vectors.get(id);
    }

    public List<Vector> getAll() {
        return new ArrayList<>(vectors.values());
    }

    public int count() {
        return vectors.size();
    }

    public void clear() {
        vectors.clear();
        lastUpdatedAt = LocalDateTime.now();
    }

    public IndexStats getStats() {
        int count = vectors.size();
        int dimension = 0;
        long memoryUsage = 0;

        if (count > 0) {
            // Retrieve dimension from the first available vector
            Vector firstVector = vectors.values().iterator().next();
            dimension = firstVector.getDimension();
            
            // Calculate memory usage (each float is 4 bytes, plus object overhead estimation)
            for (Vector v : vectors.values()) {
                memoryUsage += (long) v.getDimension() * 4 + 40; // 40 bytes overhead per Vector object
            }
            // Include map entry overhead estimate
            memoryUsage += (long) count * 32;
        }

        return IndexStats.builder()
                .workspaceId(workspaceId)
                .count(count)
                .dimension(dimension)
                .memoryUsageBytes(memoryUsage)
                .lastUpdatedAt(lastUpdatedAt)
                .build();
    }
}
