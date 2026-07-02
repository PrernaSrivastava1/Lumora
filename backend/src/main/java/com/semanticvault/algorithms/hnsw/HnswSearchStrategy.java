package com.semanticvault.algorithms.hnsw;

import com.semanticvault.algorithms.common.AbstractSearchStrategy;
import com.semanticvault.model.AlgorithmType;
import com.semanticvault.model.SearchRequest;
import com.semanticvault.model.SearchResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Placeholder strategy for the Hierarchical Navigable Small World (HNSW) search algorithm.
 */
@Component
public class HnswSearchStrategy extends AbstractSearchStrategy {

    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.HNSW;
    }

    @Override
    protected List<SearchResult> doSearch(SearchRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
