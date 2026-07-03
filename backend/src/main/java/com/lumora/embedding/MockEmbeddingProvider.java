package com.lumora.embedding;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MockEmbeddingProvider implements EmbeddingProvider {

    private final int dimension = 16;
    private final Random random = new Random();

    @Override
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            return new float[dimension];
        }
        float[] vector = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            vector[i] = random.nextFloat() * 2.0f - 1.0f; // float range [-1.0, 1.0]
        }
        // Normalize vector for distance metric consistency
        float sumOfSquares = 0.0f;
        for (float val : vector) {
            sumOfSquares += val * val;
        }
        float magnitude = (float) Math.sqrt(sumOfSquares);
        if (magnitude > 0.0f) {
            for (int i = 0; i < dimension; i++) {
                vector[i] /= magnitude;
            }
        }
        return vector;
    }

    @Override
    public String getProviderName() {
        return "MOCK";
    }

    @Override
    public int getDimension() {
        return dimension;
    }
}
