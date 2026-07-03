package com.semanticvault.algorithms.hnsw;

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

/**
 * Strategy implementing similarity search using a Hierarchical Navigable Small World (HNSW) graph index.
 */
@Component
public class HnswSearchStrategy extends AbstractSearchStrategy {

    private final VectorStore vectorStore;
    private final SearchRankingService rankingService;
    private final EmbeddingService embeddingService;
    private final ChunkRepository chunkRepository;

    public HnswSearchStrategy(VectorStore vectorStore,
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
        return AlgorithmType.HNSW;
    }

    @Override
    protected List<SearchResult> doSearch(SearchRequest request) {
        if (request.getTopK() <= 0) {
            throw new SearchException("Top-K parameter must be greater than 0");
        }

        Long workspaceId = request.getWorkspaceId();
        if (workspaceId != null && !vectorStore.getIndexManager().hasIndex(workspaceId)) {
            vectorStore.getIndexManager().loadWorkspaceVectors(workspaceId);
        }

        // Parse query vector or generate embedding
        float[] queryVals;
        try {
            queryVals = VectorUtils.parse(request.getQuery());
        } catch (Exception e) {
            queryVals = embeddingService.generateEmbedding(request.getQuery());
        }

        VectorValidator.validate(queryVals);

        List<Vector> allVectors = vectorStore.findAll(workspaceId);
        if (allVectors.isEmpty()) {
            return List.of();
        }

        // Validate query vector dimension matches stored vectors
        int expectedDimension = allVectors.get(0).getDimension();
        if (queryVals.length != expectedDimension) {
            throw new SearchException("Query vector dimension mismatch. Expected: "
                    + expectedDimension + ", got: " + queryVals.length);
        }

        DistanceCalculator calculator = DistanceCalculatorFactory.getCalculator(request.getMetric());

        // Build HNSW index dynamically
        HnswIndex index = new HnswIndex();
        index.build(allVectors, calculator);

        List<SearchResult> results = index.search(queryVals, request.getTopK(), calculator);

        // Connect back to document chunks
        for (SearchResult result : results) {
            chunkRepository.findById(result.getChunkId()).ifPresent(chunk -> {
                result.setMatchedText(chunk.getContent());
                if (chunk.getDocument() != null) {
                    result.setDocumentId(chunk.getDocument().getId());
                    result.setExplanation(result.getExplanation() + " (Doc: " + chunk.getDocument().getTitle() + ")");
                }
            });
        }

        return results;
    }
}
