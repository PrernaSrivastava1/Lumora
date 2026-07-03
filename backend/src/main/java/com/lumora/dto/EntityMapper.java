package com.lumora.dto;

import com.lumora.model.Document;
import com.lumora.model.Workspace;

/**
 * Maps JPA entities to response DTOs.
 * Ensures entities are never leaked to the API layer.
 */
public final class EntityMapper {

    private EntityMapper() {
        // utility class
    }

    public static WorkspaceResponseDto toDto(Workspace entity) {
        return WorkspaceResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .totalDocuments(entity.getTotalDocuments())
                .totalVectors(entity.getTotalVectors())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static DocumentResponseDto toDto(Document entity) {
        return DocumentResponseDto.builder()
                .id(entity.getId())
                .workspaceId(entity.getWorkspace() != null ? entity.getWorkspace().getId() : null)
                .title(entity.getTitle())
                .originalFileName(entity.getOriginalFileName())
                .fileType(entity.getFileType())
                .size(entity.getSize())
                .uploadTime(entity.getUploadTime())
                .processingStatus(entity.getProcessingStatus() != null ? entity.getProcessingStatus().name() : null)
                .totalChunks(entity.getTotalChunks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
