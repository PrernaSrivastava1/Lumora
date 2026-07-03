package com.lumora.controller;

import com.lumora.dto.AnswerResponse;
import com.lumora.dto.ApiResponse;
import com.lumora.dto.RagRequest;
import com.lumora.service.RagService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AnswerResponse>> executeChatPrompt(@Valid @RequestBody RagRequest request) {
        AnswerResponse response = ragService.performRag(request);
        return ResponseEntity.ok(ApiResponse.<AnswerResponse>builder()
                .success(true)
                .message("Answer generated successfully")
                .data(response)
                .build());
    }
}
