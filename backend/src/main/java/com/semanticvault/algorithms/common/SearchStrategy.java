package com.semanticvault.algorithms.common;

import com.semanticvault.model.AlgorithmType;
import com.semanticvault.model.SearchRequest;
import com.semanticvault.model.SearchResponse;

/**
 * Interface representing a strategy for performing vector searches.
 * Any new search algorithm should implement this interface.
 */
public interface SearchStrategy {

    /**
     * Executes the search based on the provided request.
     *
     * @param request the parameters of the search query
     * @return the search response with results and execution details
     * @throws SearchException if execution fails or inputs are invalid
     */
    SearchResponse search(SearchRequest request);

    /**
     * Returns the type of algorithm this strategy implements.
     *
     * @return the {@link AlgorithmType}
     */
    AlgorithmType getAlgorithmType();
}
