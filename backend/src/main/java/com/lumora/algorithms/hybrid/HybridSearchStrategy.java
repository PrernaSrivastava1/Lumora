package com.lumora.algorithms.hybrid;

import com.lumora.algorithms.bruteforce.BruteForceSearchStrategy;
import com.lumora.algorithms.common.AbstractSearchStrategy;
import com.lumora.algorithms.common.SearchException;
import com.lumora.algorithms.keyword.KeywordSearchStrategy;
import com.lumora.model.AlgorithmType;
import com.lumora.model.SearchRequest;
import com.lumora.model.SearchResult;
import com.lumora.repository.ChunkRepository;
import com.lumora.service.SearchRankingService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class HybridSearchStrategy extends AbstractSearchStrategy {

    private final BruteForceSearchStrategy bruteForceStrategy;
    private final KeywordSearchStrategy keywordStrategy;
    private final SearchRankingService rankingService;
    private final ChunkRepository chunkRepository;

    public HybridSearchStrategy(BruteForceSearchStrategy bruteForceStrategy,
                                KeywordSearchStrategy keywordStrategy,
                                SearchRankingService rankingService,
                                ChunkRepository chunkRepository) {
        super();
        this.bruteForceStrategy = bruteForceStrategy;
        this.keywordStrategy = keywordStrategy;
        this.rankingService = rankingService;
        this.chunkRepository = chunkRepository;
    }

    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.HYBRID;
    }

    @Override
    protected List<SearchResult> doSearch(SearchRequest request) {
        if (request.getTopK() <= 0) {
            throw new SearchException("Top-K parameter must be greater than 0");
        }

        // Run vector search (expand size slightly to fetch enough candidates to merge)
        SearchRequest vectorRequest = SearchRequest.builder()
                .query(request.getQuery())
                .workspaceId(request.getWorkspaceId())
                .algorithm(AlgorithmType.BRUTE_FORCE)
                .metric(request.getMetric())
                .topK(Math.max(50, request.getTopK() * 3))
                .build();
        List<SearchResult> vectorResults = bruteForceStrategy.search(vectorRequest).getResults();

        // Run keyword search
        SearchRequest keywordRequest = SearchRequest.builder()
                .query(request.getQuery())
                .workspaceId(request.getWorkspaceId())
                .algorithm(AlgorithmType.KEYWORD)
                .metric(request.getMetric())
                .topK(Math.max(50, request.getTopK() * 3))
                .build();
        List<SearchResult> keywordResults = keywordStrategy.search(keywordRequest).getResults();

        // Merge results
        Map<Long, Double> vectorScores = new HashMap<>();
        for (SearchResult r : vectorResults) {
            vectorScores.put(r.getChunkId(), r.getScore());
        }

        Map<Long, Double> keywordScores = new HashMap<>();
        for (SearchResult r : keywordResults) {
            keywordScores.put(r.getChunkId(), r.getScore());
        }

        // All matched chunks
        List<Long> allMatchedChunkIds = new ArrayList<>();
        for (Long id : vectorScores.keySet()) {
            allMatchedChunkIds.add(id);
        }
        for (Long id : keywordScores.keySet()) {
            if (!allMatchedChunkIds.contains(id)) {
                allMatchedChunkIds.add(id);
            }
        }

        List<SearchResult> rawCombinedResults = new ArrayList<>();

        for (Long chunkId : allMatchedChunkIds) {
            // Semantic similarity: map distance [0, 2] -> similarity [0, 1]
            double distance = vectorScores.getOrDefault(chunkId, 2.0); // 2.0 is worst distance
            double semanticSim = 1.0 - (distance / 2.0);
            if (semanticSim < 0.0) semanticSim = 0.0;

            // Keyword similarity: map score [0, inf) -> similarity [0, 1]
            double kwScore = keywordScores.getOrDefault(chunkId, 0.0);
            double keywordSim = kwScore / (5.0 + kwScore); // 5.0 is scaling factor

            // Combined score (weighted: 60% semantic, 40% keyword)
            double combinedSim = 0.6 * semanticSim + 0.4 * keywordSim;

            // Map combined similarity back to distance score (lower is better)
            double finalScore = 1.0 - combinedSim;

            String explanation = String.format("Hybrid (Semantic Sim: %.2f | Keyword Sim: %.2f | Combined: %.2f)", 
                    semanticSim, keywordSim, combinedSim);

            rawCombinedResults.add(SearchResult.builder()
                    .chunkId(chunkId)
                    .score(finalScore)
                    .explanation(explanation)
                    .build());
        }

        // Rank and slice Top K (lower score is better)
        List<SearchResult> ranked = rankingService.rank(rawCombinedResults, request.getTopK());

        // Hydrate details
        for (SearchResult result : ranked) {
            chunkRepository.findById(result.getChunkId()).ifPresent(chunk -> {
                result.setMatchedText(chunk.getContent());
                if (chunk.getDocument() != null) {
                    result.setDocumentId(chunk.getDocument().getId());
                    result.setExplanation(result.getExplanation() + " (Doc: " + chunk.getDocument().getTitle() + ")");
                }
            });
        }

        return ranked;
    }
}
