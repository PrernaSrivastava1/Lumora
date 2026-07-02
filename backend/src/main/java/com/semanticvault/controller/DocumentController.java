package com.semanticvault.controller;

import com.semanticvault.dto.ApiResponse;
import com.semanticvault.model.Document;
import com.semanticvault.service.DocumentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    public ApiResponse<Document> uploadDocument(
            @RequestParam("workspaceId") Long workspaceId,
            @RequestParam("file") MultipartFile file) {
        Document doc = documentService.uploadDocument(workspaceId, file);
        return ApiResponse.success("Document uploaded and indexed successfully", doc);
    }

    @GetMapping
    public ApiResponse<List<Document>> getDocumentsByWorkspace(@RequestParam("workspaceId") Long workspaceId) {
        List<Document> list = documentService.getDocumentsByWorkspace(workspaceId);
        return ApiResponse.success("Retrieved workspace documents", list);
    }

    @GetMapping("/{id}")
    public ApiResponse<Document> getDocumentById(@PathVariable Long id) {
        Document doc = documentService.getDocumentById(id);
        return ApiResponse.success("Retrieved document details", doc);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ApiResponse.success("Document deleted successfully", null);
    }
}
