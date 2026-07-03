package com.lumora.service;

import org.springframework.stereotype.Service;

@Service
public class TextCleaningService {

    /**
     * Cleans raw extracted text by standardizing spaces, removing excess newlines, and trimming.
     */
    public String cleanText(String text) {
        if (text == null) {
            return "";
        }
        // Normalize line breaks
        String cleaned = text.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
        // Remove excessive consecutive empty lines
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");
        // Trim each line and normalize spacing
        cleaned = cleaned.replaceAll("[ \\t]+", " ");
        return cleaned.trim();
    }
}
