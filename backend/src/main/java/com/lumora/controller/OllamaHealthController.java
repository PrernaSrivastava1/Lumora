package com.lumora.controller;

import com.lumora.dto.ApiResponse;
import com.lumora.embedding.OllamaEmbeddingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health/ollama")
public class OllamaHealthController {

    private final OllamaEmbeddingProvider ollamaEmbeddingProvider;

    @Value("${ollama.model:nomic-embed-text}")
    private String modelName;

    public OllamaHealthController(OllamaEmbeddingProvider ollamaEmbeddingProvider) {
        this.ollamaEmbeddingProvider = ollamaEmbeddingProvider;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> checkOllamaHealth() {
        boolean healthy = ollamaEmbeddingProvider.checkHealth();
        Map<String, Object> data = new HashMap<>();
        data.put("provider", "Ollama Server");
        data.put("model", modelName);
        data.put("status", healthy ? "UP" : "DOWN");

        if (healthy) {
            return ApiResponse.success("Ollama is reachable", data);
        } else {
            return ApiResponse.success("Ollama is unreachable", data);
        }
    }
}
