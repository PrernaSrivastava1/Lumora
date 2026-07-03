package com.lumora.index;

import com.lumora.algorithms.common.vector.Vector;
import com.lumora.model.IndexStats;
import com.lumora.model.VectorEmbedding;
import com.lumora.repository.VectorEmbeddingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe implementation of IndexManager.
 */
@Component
public class IndexManagerImpl implements IndexManager {

    private final VectorEmbeddingRepository embeddingRepository;
    private final Map<Long, VectorIndex> indexes = new ConcurrentHashMap<>();

    public IndexManagerImpl(VectorEmbeddingRepository embeddingRepository) {
        this.embeddingRepository = embeddingRepository;
    }

    @Override
    public VectorIndex getOrCreateIndex(Long workspaceId) {
        if (workspaceId == null) {
            throw new IllegalArgumentException("Workspace ID cannot be null");
        }
        return indexes.computeIfAbsent(workspaceId, VectorIndex::new);
    }

    @Override
    public VectorIndex getIndex(Long workspaceId) {
        if (workspaceId == null) {
            return null;
        }
        return indexes.get(workspaceId);
    }

    @Override
    public boolean deleteIndex(Long workspaceId) {
        if (workspaceId == null) {
            return false;
        }
        return indexes.remove(workspaceId) != null;
    }

    @Override
    public boolean hasIndex(Long workspaceId) {
        if (workspaceId == null) {
            return false;
        }
        return indexes.containsKey(workspaceId);
    }

    @Override
    public IndexStats getIndexStats(Long workspaceId) {
        VectorIndex index = getIndex(workspaceId);
        if (index == null) {
            throw new IllegalArgumentException("No index found for workspace ID: " + workspaceId);
        }
        return index.getStats();
    }

    @Override
    public Map<Long, IndexStats> getAllIndexStats() {
        Map<Long, IndexStats> statsMap = new HashMap<>();
        for (Map.Entry<Long, VectorIndex> entry : indexes.entrySet()) {
            statsMap.put(entry.getKey(), entry.getValue().getStats());
        }
        return statsMap;
    }

    @Override
    @Transactional(readOnly = true)
    public void loadWorkspaceVectors(Long workspaceId) {
        if (workspaceId == null) {
            throw new IllegalArgumentException("Workspace ID cannot be null");
        }

        VectorIndex index = getOrCreateIndex(workspaceId);
        index.clear();

        List<VectorEmbedding> embeddings = embeddingRepository.findByWorkspaceId(workspaceId);
        for (VectorEmbedding embedding : embeddings) {
            if (embedding.getChunk() != null) {
                Long chunkId = embedding.getChunk().getId();
                float[] values = embedding.getVectorAsFloats();
                Vector vector = new Vector(chunkId, values, embedding.getId());
                index.add(vector);
            }
        }
    }

    @Override
    public void unloadWorkspaceVectors(Long workspaceId) {
        deleteIndex(workspaceId);
    }

    @Override
    public void clearAll() {
        indexes.clear();
    }
}
