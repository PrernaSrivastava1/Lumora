package com.semanticvault.rag.chunking;

import com.semanticvault.model.DocumentChunk;

import java.util.List;

public interface ChunkingStrategy {

    /**
     * Splits text into smaller DocumentChunk partitions.
     *
     * @param text the extracted raw document text content
     * @param documentId the associated document ID
     * @return the list of chunks
     */
    List<DocumentChunk> chunk(String text, Long documentId);

    /**
     * Identifies the strategy implementation name.
     */
    String getStrategyName();
}
