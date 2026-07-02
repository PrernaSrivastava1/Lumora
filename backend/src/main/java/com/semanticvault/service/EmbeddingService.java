package com.semanticvault.service;

import com.semanticvault.embedding.EmbeddingFactory;
import com.semanticvault.embedding.EmbeddingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

    private final EmbeddingFactory embeddingFactory;

    @Value("${semanticvault.embedding.active-provider:MOCK}")
    private String activeProvider;

    public EmbeddingService(EmbeddingFactory embeddingFactory) {
        this.embeddingFactory = embeddingFactory;
    }

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
        EmbeddingProvider provider = embeddingFactory.getProvider(providerName);
        return provider.embed(text);
    }

    public int getActiveDimension() {
        return embeddingFactory.getProvider(activeProvider).getDimension();
    }

    public String getActiveProviderName() {
        return activeProvider;
    }
}
