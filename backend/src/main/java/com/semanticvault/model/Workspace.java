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
 * Represents a logical knowledge collection (e.g., project, department, scope).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workspace {

    private Long id;

    @NotBlank(message = "Workspace name cannot be blank")
    @Size(max = 100, message = "Workspace name cannot exceed 100 characters")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @NotNull(message = "Creation timestamp is required")
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Min(value = 0, message = "Total documents cannot be negative")
    @Builder.Default
    private int totalDocuments = 0;

    @Min(value = 0, message = "Total vectors cannot be negative")
    @Builder.Default
    private int totalVectors = 0;
}
