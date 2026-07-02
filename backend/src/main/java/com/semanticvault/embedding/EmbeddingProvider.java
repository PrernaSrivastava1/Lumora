package com.semanticvault.embedding;

public interface EmbeddingProvider {

    /**
     * Generates a numerical vector embedding for the given text.
     *
     * @param text the input text to embed
     * @return the float array representing the vector
     */
    float[] embed(String text);

    /**
     * Gets the unique identifier for this embedding provider.
     */
    String getProviderName();

    /**
     * Gets the output vector dimensionality of the embedding.
     */
    int getDimension();
}
