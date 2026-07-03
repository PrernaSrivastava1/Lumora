package com.lumora.embedding;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest(properties = {
    "ollama.max-retries=3",
    "ollama.retry-delay-ms=100",
    "lumora.embedding.active-provider=MOCK"
})
class OllamaIntegrationTest {

    @Autowired
    private OllamaEmbeddingProvider ollamaEmbeddingProvider;

    @MockBean(name = "ollamaRestTemplate")
    private RestTemplate restTemplate;

    @Test
    void testOllamaSuccessfulEmbedding() {
        OllamaEmbeddingProvider.OllamaResponse mockResponse = new OllamaEmbeddingProvider.OllamaResponse(
                List.of(0.1, 0.2, 0.3)
        );

        Mockito.when(restTemplate.postForObject(
                eq("http://localhost:11434/api/embeddings"),
                any(),
                eq(OllamaEmbeddingProvider.OllamaResponse.class)
        )).thenReturn(mockResponse);

        float[] vector = ollamaEmbeddingProvider.embed("Sample text");
        assertEquals(3, vector.length);
        assertEquals(0.1f, vector[0]);
        assertEquals(0.2f, vector[1]);
        assertEquals(0.3f, vector[2]);
    }

    @Test
    void testOllamaRetryOnFailures() {
        Mockito.when(restTemplate.postForObject(
                eq("http://localhost:11434/api/embeddings"),
                any(),
                eq(OllamaEmbeddingProvider.OllamaResponse.class)
        )).thenThrow(new RestClientException("Connection Timeout"));

        long start = System.currentTimeMillis();
        assertThrows(RuntimeException.class, () -> ollamaEmbeddingProvider.embed("Sample retry text"));
        long duration = System.currentTimeMillis() - start;

        // With retry delay configured at 100ms, and max retries 3,
        // it makes 3 total attempts, sleeping 2 times (2 * 100 = 200ms).
        assertTrue(duration >= 150);
        Mockito.verify(restTemplate, Mockito.times(3)).postForObject(any(String.class), any(), any());
    }
}
