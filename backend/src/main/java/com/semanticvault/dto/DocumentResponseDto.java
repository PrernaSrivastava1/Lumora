package com.semanticvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Document API responses — never exposes the entity directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponseDto {
    private Long id;
    private Long workspaceId;
    private String title;
    private String originalFileName;
    private String fileType;
    private long size;
    private LocalDateTime uploadTime;
    private String processingStatus;
    private int totalChunks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
