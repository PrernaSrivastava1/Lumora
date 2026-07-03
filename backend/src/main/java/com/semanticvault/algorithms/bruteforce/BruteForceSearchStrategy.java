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
import com.semanticvault.repository.ChunkRepository;
import com.semanticvault.repository.VectorStore;
import com.semanticvault.service.EmbeddingService;
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
    private final EmbeddingService embeddingService;
    private final ChunkRepository chunkRepository;

    public BruteForceSearchStrategy(VectorStore vectorStore,
                                    SearchRankingService rankingService,
                                    EmbeddingService embeddingService,
                                    ChunkRepository chunkRepository) {
        super();
        this.vectorStore = vectorStore;
        this.rankingService = rankingService;
        this.embeddingService = embeddingService;
        this.chunkRepository = chunkRepository;
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

        // Auto-load index if it hasn't been loaded in memory yet
        Long workspaceId = request.getWorkspaceId();
        if (workspaceId != null && !vectorStore.getIndexManager().hasIndex(workspaceId)) {
            vectorStore.getIndexManager().loadWorkspaceVectors(workspaceId);
        }

        // Parse query vector or generate embedding
        float[] queryVals;
        try {
            queryVals = VectorUtils.parse(request.getQuery());
        } catch (Exception e) {
            // If it's not a comma-separated list of floats, treat it as a natural language text query
            queryVals = embeddingService.generateEmbedding(request.getQuery());
        }

        VectorValidator.validate(queryVals);
        Vector queryVector = new Vector(null, queryVals, null);

        List<Vector> allVectors = vectorStore.findAll(workspaceId);
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

        List<SearchResult> rankedResults = rankingService.rank(rawResults, request.getTopK());

        // Connect embeddings back to document chunks
        for (SearchResult result : rankedResults) {
            chunkRepository.findById(result.getChunkId()).ifPresent(chunk -> {
                result.setMatchedText(chunk.getContent());
                if (chunk.getDocument() != null) {
                    result.setDocumentId(chunk.getDocument().getId());
                    result.setExplanation(result.getExplanation() + " (Doc: " + chunk.getDocument().getTitle() + ")");
                }
            });
        }

        return rankedResults;
    }
}
