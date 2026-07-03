package com.lumora.algorithms.common;

import com.lumora.model.SearchRequest;
import com.lumora.model.SearchResponse;
import com.lumora.model.SearchResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base class containing common functionality shared by search strategies.
 * Implements the Template Method pattern to handle validation and latency measurement.
 */
public abstract class AbstractSearchStrategy implements SearchStrategy {

    private final Validator validator;

    @org.springframework.beans.factory.annotation.Autowired
    private com.lumora.service.EmbeddingService embeddingService;

    protected AbstractSearchStrategy() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    protected AbstractSearchStrategy(Validator validator) {
        this.validator = validator;
    }

    /**
     * Template method implementing request validation, timing metrics, and delegating actual search.
     */
    @Override
    public final SearchResponse search(SearchRequest request) {
        validateRequest(request);

        long startTime = System.nanoTime();
        List<SearchResult> results = doSearch(request);
        long endTime = System.nanoTime();

        long executionTimeMs = (endTime - startTime) / 1_000_000; // Millisecond resolution

        int dimension = 0;
        try {
            dimension = com.lumora.algorithms.common.vector.VectorUtils.parse(request.getQuery()).length;
        } catch (Exception e) {
            if (embeddingService != null) {
                dimension = embeddingService.getActiveDimension();
            }
        }

        return SearchResponse.builder()
                .query(request.getQuery())
                .algorithm(getAlgorithmType())
                .metric(request.getMetric())
                .executionTime(executionTimeMs)
                .embeddingDimension(dimension)
                .resultCount(results != null ? results.size() : 0)
                .results(results)
                .build();
    }

    /**
     * Abstract hook method to implement specific search logic in subclasses.
     *
     * @param request validated search request params
     * @return list of search results
     */
    protected abstract List<SearchResult> doSearch(SearchRequest request);

    /**
     * Validates the request using the Jakarta validation API.
     *
     * @param request the request to validate
     */
    protected void validateRequest(SearchRequest request) {
        if (request == null) {
            throw new SearchException("Search request cannot be null");
        }

        Set<ConstraintViolation<SearchRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String errorMsg = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new SearchException("Search request validation failed: " + errorMsg);
        }
    }
}
