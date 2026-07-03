package com.lumora.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload model representing parameters for performing similarity queries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    @NotBlank(message = "Query text cannot be blank")
    private String query;

    @NotNull(message = "Algorithm type is required")
    private AlgorithmType algorithm;

    @NotNull(message = "Distance metric is required")
    private DistanceMetric metric;

    @Min(value = 1, message = "Top-K matches must be at least 1")
    @Builder.Default
    private int topK = 5;

    @NotNull(message = "Workspace ID is required")
    private Long workspaceId;
}
