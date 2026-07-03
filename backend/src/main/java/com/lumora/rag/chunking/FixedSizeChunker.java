package com.lumora.rag.chunking;

import com.lumora.model.Document;
import com.lumora.model.DocumentChunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FixedSizeChunker implements ChunkingStrategy {

    private final int chunkSize;

    public FixedSizeChunker() {
        this.chunkSize = 500; // Default fallback size
    }

    public FixedSizeChunker(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public String getStrategyName() {
        return "FIXED_SIZE";
    }

    @Override
    public List<DocumentChunk> chunk(String text, Long documentId) {
        List<DocumentChunk> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        int index = 0;
        int length = text.length();
        int chunkIdx = 0;

        while (index < length) {
            int end = Math.min(index + chunkSize, length);
            String chunkContent = text.substring(index, end);
            int tokens = chunkContent.trim().isEmpty() ? 0 : chunkContent.trim().split("\\s+").length;

            chunks.add(DocumentChunk.builder()
                    .document(Document.builder().id(documentId).build())
                    .chunkIndex(chunkIdx++)
                    .content(chunkContent)
                    .tokenCount(tokens)
                    .startChar(index)
                    .endChar(end)
                    .build());

            index = end;
        }

        return chunks;
    }
}
