package com.semanticvault.algorithms.common.vector;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Immutable metadata model containing context details associated with a vector.
 */
@Value
@Builder
public class VectorMetadata {
    Long documentId;
    Long chunkId;
    Long workspaceId;
    String source;
    String title;
    LocalDateTime createdAt;
}
