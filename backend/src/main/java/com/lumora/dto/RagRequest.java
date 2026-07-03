package com.lumora.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagRequest {

    @NotBlank(message = "Query cannot be blank")
    private String query;

    @NotNull(message = "WorkspaceId is required")
    private Long workspaceId;

    @Builder.Default
    private String algorithm = "AUTO";

    @Builder.Default
    private int topK = 5;

    private String llmModel;
}
