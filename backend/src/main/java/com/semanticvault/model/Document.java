package com.semanticvault.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents an uploaded source document inside a workspace.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    private Long id;

    @NotNull(message = "Workspace ID is required")
    private Long workspaceId;

    @NotBlank(message = "Document title cannot be blank")
    @Size(max = 255, message = "Document title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Original file name cannot be blank")
    @Size(max = 255, message = "Original file name cannot exceed 255 characters")
    private String originalFileName;

    @Size(max = 50, message = "File type cannot exceed 50 characters")
    private String fileType;

    @Min(value = 0, message = "File size cannot be negative")
    private long size;

    @NotNull(message = "Upload time is required")
    private LocalDateTime uploadTime;

    @NotNull(message = "Processing status is required")
    @Builder.Default
    private ProcessingStatus processingStatus = ProcessingStatus.UPLOADING;

    @Min(value = 0, message = "Total chunks count cannot be negative")
    @Builder.Default
    private int totalChunks = 0;
}
