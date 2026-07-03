package com.lumora.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Statistics for an individual vector index.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexStats {
    private Long workspaceId;
    private int count;
    private int dimension;
    private long memoryUsageBytes;
    private LocalDateTime lastUpdatedAt;
}
