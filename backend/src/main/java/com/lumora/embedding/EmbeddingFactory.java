package com.lumora.embedding;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmbeddingFactory {

    private final Map<String, EmbeddingProvider> providers = new ConcurrentHashMap<>();

    public EmbeddingFactory(List<EmbeddingProvider> providerList) {
        for (EmbeddingProvider provider : providerList) {
            providers.put(provider.getProviderName().toUpperCase(), provider);
        }
    }

    /**
     * Resolves the embedding strategy implementation by name.
     */
    public EmbeddingProvider getProvider(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Embedding provider name cannot be null");
        }
        return Optional.ofNullable(providers.get(name.toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("No embedding provider found matching: " + name));
    }
}
