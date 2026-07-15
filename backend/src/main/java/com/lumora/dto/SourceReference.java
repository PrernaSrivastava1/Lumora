package com.lumora.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceReference {
    private String documentTitle;
    private String textPreview;
    private double similarityScore;
    private Long documentId;
    private Long chunkId;
}
