package com.lumora.algorithms.kdtree;

import com.lumora.algorithms.common.AbstractSearchStrategy;
import com.lumora.algorithms.common.SearchException;
import com.lumora.algorithms.common.vector.DistanceCalculator;
import com.lumora.algorithms.common.vector.DistanceCalculatorFactory;
import com.lumora.algorithms.common.vector.Vector;
import com.lumora.algorithms.common.vector.VectorUtils;
import com.lumora.algorithms.common.vector.VectorValidator;
import com.lumora.model.AlgorithmType;
import com.lumora.model.SearchRequest;
import com.lumora.model.SearchResult;
import com.lumora.repository.ChunkRepository;
import com.lumora.repository.VectorStore;
import com.lumora.service.EmbeddingService;
import com.lumora.service.SearchRankingService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strategy implementing similarity search using a K-Dimensional Tree (KD-Tree) index.
 */
@Component
public class KdTreeSearchStrategy extends AbstractSearchStrategy {

    private final VectorStore vectorStore;
    private final SearchRankingService rankingService;
    private final EmbeddingService embeddingService;
    private final ChunkRepository chunkRepository;

    public KdTreeSearchStrategy(VectorStore vectorStore,
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
        return AlgorithmType.KD_TREE;
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

        // Build KD-Tree index
        KdTree tree = new KdTree();
        tree.build(allVectors);

        DistanceCalculator calculator = DistanceCalculatorFactory.getCalculator(request.getMetric());
        List<SearchResult> results = tree.knnSearch(queryVals, request.getTopK(), calculator, request.getMetric());

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
