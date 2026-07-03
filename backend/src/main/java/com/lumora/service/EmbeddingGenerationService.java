package com.lumora.service;

import org.springframework.stereotype.Service;

@Service
public class EmbeddingGenerationService {

    private final EmbeddingService embeddingService;

    public EmbeddingGenerationService(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    public float[] generateEmbedding(String text) {
        return embeddingService.generateEmbedding(text);
    }

    public String getActiveModelName() {
        return embeddingService.getActiveProviderName();
    }
}
