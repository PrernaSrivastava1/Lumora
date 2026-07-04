package com.lumora.service;

import com.lumora.dto.SourceReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptBuilder {

    public String buildPrompt(String query, List<SourceReference> sources) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are Lumora, an AI knowledge assistant.\n\n");
        builder.append("Answer ONLY using the retrieved context.\n");
        builder.append("If the answer is not contained in the context, say so clearly.\n");
        builder.append("Never copy long paragraphs.\n");
        builder.append("Summarize naturally.\n");
        builder.append("Explain concepts clearly.\n");
        builder.append("Use bullet points when helpful.\n");
        builder.append("Always cite which document(s) the answer came from.\n");
        builder.append("Never hallucinate.\n\n");
        
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
