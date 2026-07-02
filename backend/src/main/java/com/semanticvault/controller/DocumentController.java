package com.semanticvault.controller;

import com.semanticvault.dto.ApiResponse;
import com.semanticvault.dto.DocumentResponseDto;
import com.semanticvault.service.DocumentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ApiResponse<DocumentResponseDto> uploadDocument(
            @RequestParam("workspaceId") Long workspaceId,
            @RequestParam("file") MultipartFile file) {
        DocumentResponseDto doc = documentService.uploadDocument(workspaceId, file);
        return ApiResponse.success("Document uploaded and indexed successfully", doc);
    }

    @GetMapping
    public ApiResponse<List<DocumentResponseDto>> getDocumentsByWorkspace(@RequestParam("workspaceId") Long workspaceId) {
        List<DocumentResponseDto> list = documentService.getDocumentsByWorkspace(workspaceId);
        return ApiResponse.success("Retrieved workspace documents", list);
    }

    @GetMapping("/{id}")
    public ApiResponse<DocumentResponseDto> getDocumentById(@PathVariable Long id) {
        DocumentResponseDto doc = documentService.getDocumentById(id);
        return ApiResponse.success("Retrieved document details", doc);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ApiResponse.success("Document deleted successfully", null);
    }
}
