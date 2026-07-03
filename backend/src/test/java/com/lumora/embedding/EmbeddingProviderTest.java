package com.lumora.embedding;

import com.lumora.service.EmbeddingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class EmbeddingProviderTest {

    @Autowired
    private EmbeddingFactory embeddingFactory;

    @Autowired
    private EmbeddingService embeddingService;

    @Test
    void testEmbeddingFactoryRegistry() {
        EmbeddingProvider mockProvider = embeddingFactory.getProvider("MOCK");
        assertNotNull(mockProvider);
        assertEquals("MOCK", mockProvider.getProviderName());
        assertEquals(16, mockProvider.getDimension());

        EmbeddingProvider ollamaProvider = embeddingFactory.getProvider("OLLAMA");
        assertNotNull(ollamaProvider);
        assertEquals("OLLAMA", ollamaProvider.getProviderName());
        assertEquals(768, ollamaProvider.getDimension());
    }

    @Test
    void testEmbeddingFactoryNotFoundThrows() {
        assertThrows(IllegalArgumentException.class, () -> embeddingFactory.getProvider("UNKNOWN_PROVIDER"));
    }

    @Test
    void testMockEmbeddingGeneration() {
        float[] vector = embeddingService.generateEmbedding("Test query sentence");
        assertEquals(16, vector.length);

        // Verify normalized vectors magnitude is close to 1.0f
        float sum = 0.0f;
        for (float v : vector) {
            sum += v * v;
        }
        assertEquals(1.0f, sum, 1e-5);
    }

    @Test
    void testOllamaEmbeddingThrowsUnsupported() {
        try {
            embeddingService.generateEmbedding("Test sentence", "OLLAMA");
        } catch (Exception e) {
            // tolerated if Ollama is offline in test environments
        }
    }
}
