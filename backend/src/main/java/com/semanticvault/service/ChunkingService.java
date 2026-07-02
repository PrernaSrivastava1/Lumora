package com.semanticvault.service;

import com.semanticvault.model.DocumentChunk;
import com.semanticvault.rag.chunking.ChunkingStrategy;
import com.semanticvault.repository.ChunkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChunkingService {

    private final ChunkRepository chunkRepository;
    private final List<ChunkingStrategy> chunkers;

    @Value("${semanticvault.chunking.default-strategy:FIXED_SIZE}")
    private String defaultStrategy;

    public ChunkingService(ChunkRepository chunkRepository, List<ChunkingStrategy> chunkers) {
        this.chunkRepository = chunkRepository;
        this.chunkers = chunkers;
    }

    /**
     * Chunk text using the selected strategy and save chunks in the repository.
     */
    public List<DocumentChunk> chunkAndStore(Long documentId, String text, String strategyName) {
        String strategyToUse = (strategyName == null || strategyName.isBlank()) ? defaultStrategy : strategyName;

        ChunkingStrategy chunker = chunkers.stream()
                .filter(c -> c.getStrategyName().equalsIgnoreCase(strategyToUse))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No chunking strategy found matching: " + strategyToUse));

        List<DocumentChunk> chunks = chunker.chunk(text, documentId);
        return chunkRepository.saveAll(chunks);
    }

    public List<DocumentChunk> getChunksForDocument(Long documentId) {
        return chunkRepository.findByDocumentId(documentId);
    }

    public void deleteChunksForDocument(Long documentId) {
        chunkRepository.deleteByDocumentId(documentId);
    }
}
