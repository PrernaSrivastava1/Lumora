package com.lumora.service;

import com.lumora.dto.SourceReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptBuilder {

    public String buildPrompt(String query, List<SourceReference> sources) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are Lumora AI, an advanced retrieval-augmented assistant.\n");
        builder.append("Answer the user's question relying ONLY on the provided document contexts below. ");
        builder.append("If the context does not contain the answer, say honestly that you do not know.\n\n");
        
        builder.append("=== CONTEXT DOCUMENTS ===\n");
        for (int i = 0; i < sources.size(); i++) {
            SourceReference src = sources.get(i);
            builder.append(String.format("[%d] Document: %s (Relevance: %.2f%%)\n", 
                    i + 1, src.getDocumentTitle(), src.getSimilarityScore() * 100));
            builder.append("Content: ").append(src.getTextPreview().trim()).append("\n\n");
        }
        builder.append("=========================\n\n");
        
        builder.append("User Question: ").append(query).append("\n");
        builder.append("Answer (concise, clear, and markdown formatted):");
        
        return builder.toString();
    }
}
