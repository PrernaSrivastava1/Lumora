package com.lumora.rag.chunking;

import com.lumora.model.Document;
import com.lumora.model.DocumentChunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SemanticChunker implements ChunkingStrategy {

    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[^.!?]+([.!?]+|$)");

    @Override
    public String getStrategyName() {
        return "SEMANTIC";
    }

    @Override
    public List<DocumentChunk> chunk(String text, Long documentId) {
        List<DocumentChunk> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        Matcher matcher = SENTENCE_PATTERN.matcher(text);
        List<SentenceInfo> sentences = new ArrayList<>();
        while (matcher.find()) {
            String sentence = matcher.group();
            if (!sentence.trim().isEmpty()) {
                sentences.add(new SentenceInfo(sentence, matcher.start(), matcher.end()));
            }
        }

        int sentencesPerChunk = 3;
        int chunkIdx = 0;
        for (int i = 0; i < sentences.size(); i += sentencesPerChunk) {
            int endIdx = Math.min(i + sentencesPerChunk, sentences.size());
            StringBuilder contentBuilder = new StringBuilder();
            int startChar = sentences.get(i).start;
            int endChar = sentences.get(endIdx - 1).end;

            for (int j = i; j < endIdx; j++) {
                contentBuilder.append(sentences.get(j).text);
            }

            String chunkContent = contentBuilder.toString();
            int tokens = chunkContent.trim().isEmpty() ? 0 : chunkContent.trim().split("\\s+").length;

            chunks.add(DocumentChunk.builder()
                    .document(Document.builder().id(documentId).build())
                    .chunkIndex(chunkIdx++)
                    .content(chunkContent)
                    .tokenCount(tokens)
                    .startChar(startChar)
                    .endChar(endChar)
                    .build());
        }

        return chunks;
    }

    private static class SentenceInfo {
        String text;
        int start;
        int end;

        SentenceInfo(String text, int start, int end) {
            this.text = text;
            this.start = start;
            this.end = end;
        }
    }
}
