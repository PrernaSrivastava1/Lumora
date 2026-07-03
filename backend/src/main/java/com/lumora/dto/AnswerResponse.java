package com.lumora.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponse {
    private String answer;
    private List<SourceReference> sources;
    private String algorithmUsed;
    private long responseTimeMs;
    private Integer promptTokens;
    private Integer answerTokens;
    private Integer contextSizeChars;
    private String finalPromptSent;
}
