package com.semanticvault.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a specific granular text chunk extracted from a parent Document.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunk {

    private Long id;

    @NotNull(message = "Document ID is required")
    private Long documentId;

    @Min(value = 0, message = "Chunk index cannot be negative")
    private int chunkIndex;

    @NotBlank(message = "Chunk content cannot be empty")
    private String content;

    private Long embeddingId;

    @Min(value = 0, message = "Token count cannot be negative")
    private int tokenCount;

    private int startChar;

    private int endChar;
}
