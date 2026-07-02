package com.semanticvault.service;

import com.semanticvault.model.Document;
import com.semanticvault.model.ProcessingStatus;
import com.semanticvault.model.Workspace;
import com.semanticvault.repository.DocumentRepository;
import com.semanticvault.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProcessingService processingService;
    private final ChunkingService chunkingService;
    private final Path uploadPath;

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("pdf", "docx", "txt", "md");

    public DocumentService(DocumentRepository documentRepository,
                           WorkspaceRepository workspaceRepository,
                           ProcessingService processingService,
                           ChunkingService chunkingService) {
        this.documentRepository = documentRepository;
        this.workspaceRepository = workspaceRepository;
        this.processingService = processingService;
        this.chunkingService = chunkingService;
        this.uploadPath = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload folder on disk", e);
        }
    }

    public List<Document> getDocumentsByWorkspace(Long workspaceId) {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new IllegalArgumentException("Workspace not found with ID: " + workspaceId);
        }
        return documentRepository.findByWorkspaceId(workspaceId);
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + id));
    }

    public Document uploadDocument(Long workspaceId, MultipartFile file) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found with ID: " + workspaceId));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported file type. Supported types: PDF, DOCX, TXT, MD");
        }

        // Store file locally
        String storedFileName = System.currentTimeMillis() + "_" + originalFilename;
        Path targetLocation = this.uploadPath.resolve(storedFileName);

        // Save metadata initially
        Document doc = Document.builder()
                .workspaceId(workspaceId)
                .title(originalFilename)
                .originalFileName(storedFileName) // save actual stored disk path filename
                .fileType(extension.toUpperCase())
                .size(file.getSize())
                .uploadTime(LocalDateTime.now())
                .processingStatus(ProcessingStatus.READY)
                .totalChunks(0)
                .build();

        Document savedDoc = documentRepository.save(doc);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            // Process document (extract text and metadata)
            com.semanticvault.document.ParsedDocument parsed = processingService.processDocument(targetLocation, extension.toUpperCase());
            // Perform text chunking & save in chunks repository
            java.util.List<com.semanticvault.model.DocumentChunk> chunks = chunkingService.chunkAndStore(savedDoc.getId(), parsed.getText(), null);
            // Update document with chunks count
            savedDoc.setTotalChunks(chunks.size());
            documentRepository.save(savedDoc);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store or process file", e);
        }

        // Update workspace metadata stats
        workspace.setTotalDocuments(workspace.getTotalDocuments() + 1);
        workspaceRepository.save(workspace);

        return savedDoc;
    }

    public void deleteDocument(Long id) {
        Document doc = getDocumentById(id);
        
        // Delete local file from disk
        Path filePath = this.uploadPath.resolve(doc.getOriginalFileName());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log warning but proceed with metadata deletion
            System.err.println("Warning: could not delete disk file " + filePath + ": " + e.getMessage());
        }

        // Delete text chunks from store
        chunkingService.deleteChunksForDocument(id);

        // Delete metadata
        documentRepository.deleteById(id);

        // Update workspace stats
        workspaceRepository.findById(doc.getWorkspaceId()).ifPresent(workspace -> {
            workspace.setTotalDocuments(Math.max(0, workspace.getTotalDocuments() - 1));
            workspaceRepository.save(workspace);
        });
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastIndex + 1);
    }
}
