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
    private static final Pattern HEADING_PATTERN = Pattern.compile("^(#+\\s+.*|\\d+\\.\\s+[A-Z].*|[A-Z][a-zA-Z\\s]{3,50}:?)$");

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

        // Extract sentences
        Matcher matcher = SENTENCE_PATTERN.matcher(text);
        List<SentenceInfo> sentences = new ArrayList<>();
        while (matcher.find()) {
            String sentenceText = matcher.group();
            if (!sentenceText.trim().isEmpty()) {
                sentences.add(new SentenceInfo(sentenceText, matcher.start(), matcher.end()));
            }
        }

        List<SentenceInfo> currentChunkSentences = new ArrayList<>();
        int currentWordCount = 0;
        int chunkIdx = 0;

        for (int i = 0; i < sentences.size(); i++) {
            SentenceInfo sentence = sentences.get(i);
            String trimmedSentence = sentence.text.trim();
            int sentenceWords = trimmedSentence.split("\\s+").length;

            boolean isHeading = HEADING_PATTERN.matcher(trimmedSentence).matches();

            // If we hit a heading and we already have some content, or if we exceed our token target (300 words)
            if ((isHeading && !currentChunkSentences.isEmpty()) || (currentWordCount >= 300 && !currentChunkSentences.isEmpty())) {
                // Build current chunk
                chunks.add(createChunk(currentChunkSentences, documentId, chunkIdx++));

                // Implement overlap: keep last 2 sentences from the previous chunk
                List<SentenceInfo> overlapSentences = new ArrayList<>();
                int overlapWords = 0;
                for (int j = Math.max(0, currentChunkSentences.size() - 2); j < currentChunkSentences.size(); j++) {
                    SentenceInfo os = currentChunkSentences.get(j);
                    overlapSentences.add(os);
                    overlapWords += os.text.trim().split("\\s+").length;
                }

                currentChunkSentences = new ArrayList<>(overlapSentences);
                currentWordCount = overlapWords;
            }

            currentChunkSentences.add(sentence);
            currentWordCount += sentenceWords;
        }

        if (!currentChunkSentences.isEmpty()) {
            chunks.add(createChunk(currentChunkSentences, documentId, chunkIdx++));
        }

        return chunks;
    }

    private DocumentChunk createChunk(List<SentenceInfo> sentences, Long documentId, int chunkIndex) {
        StringBuilder contentBuilder = new StringBuilder();
        int startChar = sentences.get(0).start;
        int endChar = sentences.get(sentences.size() - 1).end;

        for (SentenceInfo s : sentences) {
            contentBuilder.append(s.text);
        }

        String content = contentBuilder.toString();
        int tokens = content.trim().isEmpty() ? 0 : content.trim().split("\\s+").length;

        return DocumentChunk.builder()
                .document(Document.builder().id(documentId).build())
                .chunkIndex(chunkIndex)
                .content(content)
                .tokenCount(tokens)
                .startChar(startChar)
                .endChar(endChar)
                .build();
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
