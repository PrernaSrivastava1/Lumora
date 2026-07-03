package com.lumora.service;

import com.lumora.model.DocumentChunk;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SemanticChunkingService {

    private final ChunkingService chunkingService;

    public SemanticChunkingService(ChunkingService chunkingService) {
        this.chunkingService = chunkingService;
    }

    public List<DocumentChunk> chunkText(Long documentId, String text) {
        return chunkingService.chunkAndStore(documentId, text, null);
    }
}
