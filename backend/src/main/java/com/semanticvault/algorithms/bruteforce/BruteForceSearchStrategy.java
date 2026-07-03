package com.semanticvault.algorithms.bruteforce;

import com.semanticvault.algorithms.common.AbstractSearchStrategy;
import com.semanticvault.algorithms.common.SearchException;
import com.semanticvault.algorithms.common.vector.DistanceCalculator;
import com.semanticvault.algorithms.common.vector.DistanceCalculatorFactory;
import com.semanticvault.algorithms.common.vector.Vector;
import com.semanticvault.algorithms.common.vector.VectorUtils;
import com.semanticvault.algorithms.common.vector.VectorValidator;
import com.semanticvault.model.AlgorithmType;
import com.semanticvault.model.SearchRequest;
import com.semanticvault.model.SearchResult;
import com.semanticvault.repository.VectorStore;
import com.semanticvault.service.SearchRankingService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy implementing the exact K-Nearest Neighbor (KNN) brute force search algorithm.
 * Performs an exhaustive scan over all stored vectors.
 */
@Component
public class BruteForceSearchStrategy extends AbstractSearchStrategy {

    private final VectorStore vectorStore;
    private final SearchRankingService rankingService;

    public BruteForceSearchStrategy(VectorStore vectorStore, SearchRankingService rankingService) {
        super();
        this.vectorStore = vectorStore;
        this.rankingService = rankingService;
    }

    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.BRUTE_FORCE;
    }

    @Override
    protected List<SearchResult> doSearch(SearchRequest request) {
        if (request.getTopK() <= 0) {
            throw new SearchException("Top-K parameter must be greater than 0");
        }

        // Parse query vector values from the textual query field
        float[] queryVals = VectorUtils.parse(request.getQuery());
        VectorValidator.validate(queryVals);

        Vector queryVector = new Vector(null, queryVals, null);

        List<Vector> allVectors = vectorStore.findAll(request.getWorkspaceId());
        if (allVectors.isEmpty()) {
            return List.of();
        }

        // Validate query vector dimension matches stored vectors
        int expectedDimension = allVectors.get(0).getDimension();
        if (queryVector.getDimension() != expectedDimension) {
            throw new SearchException("Query vector dimension mismatch. Expected: "
                    + expectedDimension + ", got: " + queryVector.getDimension());
        }

        DistanceCalculator calculator = DistanceCalculatorFactory.getCalculator(request.getMetric());

        List<SearchResult> rawResults = allVectors.stream()
                .map(v -> {
                    double distance = calculator.calculate(queryVector, v);
                    return SearchResult.builder()
                            .chunkId(v.getId())
                            .score(distance)
                            .explanation("Exact distance calculated via " + request.getMetric())
                            .build();
                })
                .collect(Collectors.toList());

        return rankingService.rank(rawResults, request.getTopK());
    }
}
