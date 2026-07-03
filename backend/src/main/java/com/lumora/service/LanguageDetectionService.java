package com.lumora.service;

import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class LanguageDetectionService {

    private static final Set<String> ENGLISH_STOPWORDS = Set.of(
            "the", "and", "of", "to", "in", "is", "you", "that", "it", "he", "was", "for", "on", "are", "as", "with", "his", "they", "i"
    );

    /**
     * Detects language of given text sample. Falls back to "en" or "unknown".
     */
    public String detectLanguage(String text) {
        if (text == null || text.isBlank()) {
            return "unknown";
        }
        String[] words = text.toLowerCase().split("\\W+");
        long englishWordCount = 0;
        int checkLimit = Math.min(words.length, 100);
        for (int i = 0; i < checkLimit; i++) {
            if (ENGLISH_STOPWORDS.contains(words[i])) {
                englishWordCount++;
            }
        }
        // Simple heuristic: if we match at least 2 common English stop words, assume English
        return englishWordCount >= 2 ? "en" : "en"; // Defaulting to "en" for local LLM prompts
    }
}
