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

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.llm-model:llama3}")
    private String defaultLlmModel;

    public RagService(SearchContext searchContext,
                      DocumentRepository documentRepository,
                      PromptBuilder promptBuilder,
                      @Qualifier("ollamaRestTemplate") RestTemplate restTemplate) {
        this.searchContext = searchContext;
        this.documentRepository = documentRepository;
        this.promptBuilder = promptBuilder;
        this.restTemplate = restTemplate;
    }

    public AnswerResponse performRag(RagRequest request) {
        long startTime = System.currentTimeMillis();

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
                        .build());
            }
        }

        // 2. Build system prompt
        String prompt = promptBuilder.buildPrompt(request.getQuery(), sources);

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
                answer = "Error: Received empty response from local Ollama generation service.";
            }
        } catch (Exception e) {
            logger.warn("Ollama LLM connection failed. Falling back to synthetic mock response. Error: {}", e.getMessage());
            answer = "*(Ollama Offline Fallback)*\n\nBased on your documents, here is the retrieved information:\n"
                    + (sources.isEmpty() ? "No document context matched your query." 
                       : "1. " + sources.get(0).getTextPreview());
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
                .build();
    }
}
