package com.lumora.algorithms.keyword;

import com.lumora.algorithms.common.AbstractSearchStrategy;
import com.lumora.algorithms.common.SearchException;
import com.lumora.model.AlgorithmType;
import com.lumora.model.Document;
import com.lumora.model.DocumentChunk;
import com.lumora.model.SearchRequest;
import com.lumora.model.SearchResult;
import com.lumora.repository.ChunkRepository;
import com.lumora.service.SearchRankingService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class KeywordSearchStrategy extends AbstractSearchStrategy {

    private final ChunkRepository chunkRepository;
    private final SearchRankingService rankingService;

    public KeywordSearchStrategy(ChunkRepository chunkRepository, SearchRankingService rankingService) {
        super();
        this.chunkRepository = chunkRepository;
        this.rankingService = rankingService;
    }

    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.KEYWORD;
    }

    @Override
    protected List<SearchResult> doSearch(SearchRequest request) {
        if (request.getTopK() <= 0) {
            throw new SearchException("Top-K parameter must be greater than 0");
        }

        Long workspaceId = request.getWorkspaceId();
        List<DocumentChunk> allChunks = chunkRepository.findByDocumentWorkspaceId(workspaceId);
        if (allChunks.isEmpty()) {
            return List.of();
        }

        String query = request.getQuery().toLowerCase();
        String[] queryKeywords = query.split("\\s+");

        java.util.Set<String> expandedKeywords = new java.util.HashSet<>();
        for (String keyword : queryKeywords) {
            expandedKeywords.add(keyword);
            if (keyword.equals("backend") || keyword.equals("server")) {
                expandedKeywords.addAll(java.util.Arrays.asList("java", "spring", "springboot", "api", "rest", "backend", "server", "database", "sql"));
            } else if (keyword.equals("frontend") || keyword.equals("ui") || keyword.equals("ux")) {
                expandedKeywords.addAll(java.util.Arrays.asList("react", "typescript", "javascript", "css", "html", "vite", "tailwind", "ui", "ux", "frontend"));
            } else if (keyword.equals("candidate") || keyword.equals("resume") || keyword.equals("cv") || keyword.equals("applicant")) {
                expandedKeywords.addAll(java.util.Arrays.asList("resume", "cv", "skills", "experience", "education", "work", "applicant", "candidate"));
            } else if (keyword.equals("skills") || keyword.equals("technologies") || keyword.equals("stack")) {
                expandedKeywords.addAll(java.util.Arrays.asList("technologies", "experience", "stack", "languages", "tools", "skills"));
            } else if (keyword.equals("compare") || keyword.equals("versus")) {
                expandedKeywords.addAll(java.util.Arrays.asList("difference", "versus", "comparison", "similarities", "compare"));
            }
        }

        List<SearchResult> rawResults = new ArrayList<>();

        for (DocumentChunk chunk : allChunks) {
            String content = chunk.getContent().toLowerCase();
            double matchScore = 0.0;

            // 1. Keyword overlap (Frequency / occurrence)
            for (String keyword : expandedKeywords) {
                if (keyword.length() < 2) continue; // skip single character words
                int idx = 0;
                int occurrences = 0;
                while ((idx = content.indexOf(keyword, idx)) != -1) {
                    occurrences++;
                    idx += keyword.length();
                }
                double weight = java.util.Arrays.asList(queryKeywords).contains(keyword) ? 1.0 : 0.5;
                matchScore += (occurrences * weight);
            }

            if (matchScore == 0.0) {
                continue; // no matching keywords
            }

            Document doc = chunk.getDocument();
            double titleBoost = 0.0;
            double recencyBoost = 0.0;

            if (doc != null) {
                // 2. Title match boost
                String title = doc.getTitle().toLowerCase();
                for (String keyword : queryKeywords) {
                    if (keyword.length() >= 2 && title.contains(keyword)) {
                        titleBoost += 5.0; // Significant boost for title matches
                    }
                }

                // 3. Recency boost (up to 2.0 max boost for extremely fresh documents)
                if (doc.getUploadTime() != null) {
                    long daysDiff = Math.abs(ChronoUnit.DAYS.between(doc.getUploadTime(), LocalDateTime.now()));
                    recencyBoost = 2.0 / (1.0 + (daysDiff / 30.0)); // falls off monthly
                }
            }

            double finalScore = matchScore + titleBoost + recencyBoost;

            rawResults.add(SearchResult.builder()
                    .chunkId(chunk.getId())
                    .score(finalScore)
                    .documentId(doc != null ? doc.getId() : null)
                    .matchedText(chunk.getContent())
                    .explanation(String.format("Keyword Matches: %.1f | Title Boost: %.1f | Recency Boost: %.1f", 
                            matchScore, titleBoost, recencyBoost))
                    .build());
        }

        // Rank and sort descending (higher keyword score is better)
        rawResults.sort(Comparator.comparingDouble(SearchResult::getScore).reversed());

        return rawResults.stream()
                .limit(request.getTopK())
                .collect(Collectors.toList());
    }
}
