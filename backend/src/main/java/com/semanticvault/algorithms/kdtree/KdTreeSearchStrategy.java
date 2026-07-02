package com.semanticvault.algorithms.kdtree;

import com.semanticvault.algorithms.common.AbstractSearchStrategy;
import com.semanticvault.model.AlgorithmType;
import com.semanticvault.model.SearchRequest;
import com.semanticvault.model.SearchResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Placeholder strategy for the KD-Tree search algorithm.
 */
@Component
public class KdTreeSearchStrategy extends AbstractSearchStrategy {

    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.KD_TREE;
    }

    @Override
    protected List<SearchResult> doSearch(SearchRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
