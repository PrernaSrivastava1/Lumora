package com.lumora.service;

import com.lumora.embedding.EmbeddingFactory;
import com.lumora.embedding.EmbeddingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

    private final EmbeddingFactory embeddingFactory;

    @Value("${Lumora.embedding.active-provider:MOCK}")
    private String activeProvider;

    public EmbeddingService(EmbeddingFactory embeddingFactory) {
        this.embeddingFactory = embeddingFactory;
    }

    private final java.util.Map<String, float[]> embeddingCache = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Generates embedding using the default active provider.
     */
    public float[] generateEmbedding(String text) {
        return generateEmbedding(text, activeProvider);
    }

    /**
     * Generates embedding using the specified provider.
     */
    public float[] generateEmbedding(String text, String providerName) {
        if (text == null) {
            return new float[0];
        }
        String key = providerName + "::" + text.trim();
        return embeddingCache.computeIfAbsent(key, k -> {
            EmbeddingProvider provider = embeddingFactory.getProvider(providerName);
            return provider.embed(text);
        }).clone();
    }

    public int getActiveDimension() {
        return embeddingFactory.getProvider(activeProvider).getDimension();
    }

    public String getActiveProviderName() {
        return activeProvider;
    }
}
