package com.lumora.service;

import com.lumora.algorithms.common.vector.Vector;
import com.lumora.index.IndexManager;
import com.lumora.index.VectorIndex;
import org.springframework.stereotype.Service;

@Service
public class VectorIndexService {

    private final IndexManager indexManager;

    public VectorIndexService(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    public VectorIndex getOrCreateIndex(Long workspaceId) {
        return indexManager.getOrCreateIndex(workspaceId);
    }
}
