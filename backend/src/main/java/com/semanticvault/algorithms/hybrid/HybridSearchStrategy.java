package com.semanticvault.algorithms.hybrid;

import com.semanticvault.algorithms.common.AbstractSearchStrategy;
import com.semanticvault.model.AlgorithmType;
import com.semanticvault.model.SearchRequest;
import com.semanticvault.model.SearchResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Placeholder strategy for the Hybrid search algorithm.
 */
@Component
public class HybridSearchStrategy extends AbstractSearchStrategy {

    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.HYBRID;
    }

    @Override
    protected List<SearchResult> doSearch(SearchRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
