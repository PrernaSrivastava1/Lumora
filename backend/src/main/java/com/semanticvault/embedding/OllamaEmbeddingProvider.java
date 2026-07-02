package com.semanticvault.embedding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class OllamaEmbeddingProvider implements EmbeddingProvider {

    private final RestTemplate restTemplate;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${ollama.model:nomic-embed-text}")
    private String modelName;

    @Value("${ollama.max-retries:3}")
    private int maxRetries;

    @Value("${ollama.retry-delay-ms:1000}")
    private int retryDelayMs;

    public OllamaEmbeddingProvider(@Qualifier("ollamaRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public float[] embed(String text) {
        String endpoint = baseUrl + "/api/embeddings";
        OllamaRequest request = new OllamaRequest(modelName, text);

        int attempt = 0;
        while (true) {
            attempt++;
            try {
                OllamaResponse response = restTemplate.postForObject(endpoint, request, OllamaResponse.class);
                if (response != null && response.getEmbedding() != null) {
                    List<Double> list = response.getEmbedding();
                    float[] vector = new float[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        vector[i] = list.get(i).floatValue();
                    }
                    return vector;
                }
                throw new RestClientException("Received empty response payload from Ollama endpoint");
            } catch (Exception e) {
                if (attempt >= maxRetries) {
                    throw new RuntimeException("Ollama embedding call failed after " + maxRetries + " attempts: " + e.getMessage(), e);
                }
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Embedding retry backoff was interrupted", ie);
                }
            }
        }
    }

    @Override
    public String getProviderName() {
        return "OLLAMA";
    }

    @Override
    public int getDimension() {
        return 768; // nomic-embed-text standard dimension count
    }

    public boolean checkHealth() {
        try {
            String res = restTemplate.getForObject(baseUrl + "/", String.class);
            return res != null && res.toLowerCase().contains("ollama");
        } catch (Exception e) {
            return false;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class OllamaRequest {
        private String model;
        private String prompt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class OllamaResponse {
        private List<Double> embedding;
    }
}
