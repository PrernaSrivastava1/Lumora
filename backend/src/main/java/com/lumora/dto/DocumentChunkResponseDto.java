package com.lumora.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunkResponseDto {
    private Long id;
    private int chunkIndex;
    private String content;
    private int startChar;
    private int endChar;
}
