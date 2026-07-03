package com.lumora.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single hit matched by a vector search query.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

    private Long documentId;

    private Long chunkId;

    private double score;

    private String matchedText;

    private String explanation;
}
