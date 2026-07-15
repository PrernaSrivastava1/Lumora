package com.lumora.service;

import com.lumora.dto.*;
import com.lumora.model.*;
import com.lumora.repository.DocumentRepository;
import com.lumora.algorithms.common.SearchContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.lumora.repository.VectorStore;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagService.class);

    private final SearchContext searchContext;
    private final DocumentRepository documentRepository;
    private final PromptBuilder promptBuilder;
    private final RestTemplate restTemplate;
    private final VectorStore vectorStore;
    private final com.lumora.analytics.SearchAnalyticsService analyticsService;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.llm-model:llama3}")
    private String defaultLlmModel;

    public RagService(SearchContext searchContext,
                      DocumentRepository documentRepository,
                      PromptBuilder promptBuilder,
                      @Qualifier("ollamaRestTemplate") RestTemplate restTemplate,
                      VectorStore vectorStore,
                      com.lumora.analytics.SearchAnalyticsService analyticsService) {
        this.searchContext = searchContext;
        this.documentRepository = documentRepository;
        this.promptBuilder = promptBuilder;
        this.restTemplate = restTemplate;
        this.vectorStore = vectorStore;
        this.analyticsService = analyticsService;
    }

    @Transactional
    public AnswerResponse performRag(RagRequest request) {
        if (vectorStore.count(request.getWorkspaceId()) == 0) {
            throw new IllegalArgumentException("No indexed documents found in this workspace.");
        }

        long startTime = System.currentTimeMillis();
        analyticsService.incrementAiQuestionsCount(request.getWorkspaceId());

        // 1. Semantic search for Top-K chunks
        SearchRequest searchRequest = SearchRequest.builder()
                .query(request.getQuery())
                .workspaceId(request.getWorkspaceId())
                .algorithm(AlgorithmType.valueOf(request.getAlgorithm().toUpperCase()))
                .metric(DistanceMetric.COSINE)
                .topK(request.getTopK())
                .build();

        SearchResponse searchResponse = searchContext.search(searchRequest);

        List<SourceReference> sources = new ArrayList<>();
        if (searchResponse.getResults() != null) {
            for (SearchResult result : searchResponse.getResults()) {
                String docTitle = documentRepository.findById(result.getDocumentId())
                        .map(Document::getTitle)
                        .orElse("Unknown Document");

                sources.add(SourceReference.builder()
                        .documentTitle(docTitle)
                        .textPreview(result.getMatchedText())
                        .similarityScore(result.getScore())
                        .documentId(result.getDocumentId())
                        .chunkId(result.getChunkId())
                        .build());
            }
        }

        // 2. Build system prompt
        String prompt = promptBuilder.buildPrompt(request.getQuery(), sources, request.getHistory());

        // 3. Invoke local Ollama generator
        String modelName = request.getLlmModel() != null ? request.getLlmModel() : defaultLlmModel;
        String answer;
        Integer promptTokens = null;
        Integer answerTokens = null;
        try {
            String endpoint = ollamaBaseUrl + "/api/generate";
            Map<String, Object> payload = Map.of(
                    "model", modelName,
                    "prompt", prompt,
                    "stream", false
            );

            Map<?, ?> response = restTemplate.postForObject(endpoint, payload, Map.class);
            if (response != null && response.containsKey("response")) {
                answer = (String) response.get("response");
                if (response.containsKey("prompt_eval_count")) {
                    promptTokens = ((Number) response.get("prompt_eval_count")).intValue();
                }
                if (response.containsKey("eval_count")) {
                    answerTokens = ((Number) response.get("eval_count")).intValue();
                }
            } else {
                answer = generateLocalFallbackResponse(request.getQuery(), sources);
            }
        } catch (Exception e) {
            logger.warn("Ollama LLM connection failed. Falling back to dynamic mock response. Error: {}", e.getMessage());
            answer = generateLocalFallbackResponse(request.getQuery(), sources);
        }

        long endTime = System.currentTimeMillis();

        return AnswerResponse.builder()
                .answer(answer)
                .sources(sources)
                .algorithmUsed(searchResponse.getAlgorithm() != null ? searchResponse.getAlgorithm().name() : "AUTO")
                .responseTimeMs(endTime - startTime)
                .promptTokens(promptTokens)
                .answerTokens(answerTokens)
                .contextSizeChars(prompt.length())
                .finalPromptSent(prompt)
                .embeddingDimension(searchResponse.getEmbeddingDimension())
                .totalVectorsSearched(vectorStore.count(request.getWorkspaceId()))
                .build();
    }

    private String generateLocalFallbackResponse(String query, List<SourceReference> sources) {
        if (sources == null || sources.isEmpty()) {
            return "[Local Demo Assistant Mode]\n\nI couldn't find any relevant documents in the workspace for: \"" + query + "\". Please upload files containing these keywords.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[Local RAG Simulation Mode - Offline]\n\n");
        sb.append("Here is the relevant information extracted from your workspace documents matching your query:\n\n");

        for (int i = 0; i < Math.min(sources.size(), 3); i++) {
            SourceReference src = sources.get(i);
            sb.append("### Source #").append(i + 1).append(": ").append(src.getDocumentTitle())
              .append(" (Relevance: ").append(Math.round(src.getSimilarityScore() * 100)).append("%)\n");
            sb.append("> \"").append(src.getTextPreview().trim()).append("\"\n\n");
        }

        sb.append("--- \n*Note: Ollama is currently offline. Start it using 'ollama serve' to enable LLM-generated synthesis.*");
        return sb.toString();
    }
}
