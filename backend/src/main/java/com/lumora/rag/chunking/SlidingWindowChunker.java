package com.lumora.rag.chunking;

import com.lumora.model.Document;
import com.lumora.model.DocumentChunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SlidingWindowChunker implements ChunkingStrategy {

    private final int chunkSize;
    private final int overlap;

    public SlidingWindowChunker() {
        this.chunkSize = 500;
        this.overlap = 100;
    }

    public SlidingWindowChunker(int chunkSize, int overlap) {
        if (overlap >= chunkSize) {
            throw new IllegalArgumentException("Overlap size must be strictly smaller than chunk size");
        }
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    @Override
    public String getStrategyName() {
        return "SLIDING_WINDOW";
    }

    @Override
    public List<DocumentChunk> chunk(String text, Long documentId) {
        List<DocumentChunk> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        int length = text.length();
        int index = 0;
        int chunkIdx = 0;
        int step = chunkSize - overlap;

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

            if (end == length) {
                break;
            }
            index += step;
        }

        return chunks;
    }
}
