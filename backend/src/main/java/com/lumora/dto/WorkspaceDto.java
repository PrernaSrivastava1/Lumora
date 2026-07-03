package com.lumora.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceDto {

    @NotBlank(message = "Workspace name is required")
    @Size(max = 100, message = "Workspace name must not exceed 100 characters")
    private String name;

    @Size(max = 255, message = "Workspace description must not exceed 255 characters")
    private String description;
}
