package com.lumora.algorithms.hybrid;

import com.lumora.algorithms.common.AbstractSearchStrategy;
import com.lumora.model.AlgorithmType;
import com.lumora.model.SearchRequest;
import com.lumora.model.SearchResult;
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
