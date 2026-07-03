package com.lumora.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Workspace API responses — never exposes the entity directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceResponseDto {
    private Long id;
    private String name;
    private String description;
    private int totalDocuments;
    private int totalVectors;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
