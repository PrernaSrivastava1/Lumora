package com.lumora.service;

import com.lumora.dto.DocumentResponseDto;
import com.lumora.dto.EntityMapper;
import com.lumora.model.Document;
import com.lumora.model.ProcessingStatus;
import com.lumora.model.Workspace;
import com.lumora.repository.DocumentRepository;
import com.lumora.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProcessingService processingService;
    private final ChunkingService chunkingService;
    private final DocumentProcessingPipeline documentProcessingPipeline;
    private final Path uploadPath;

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("pdf", "docx", "txt", "md");

    public DocumentService(DocumentRepository documentRepository,
                           WorkspaceRepository workspaceRepository,
                           ProcessingService processingService,
                           ChunkingService chunkingService,
                           DocumentProcessingPipeline documentProcessingPipeline) {
        this.documentRepository = documentRepository;
        this.workspaceRepository = workspaceRepository;
        this.processingService = processingService;
        this.chunkingService = chunkingService;
        this.documentProcessingPipeline = documentProcessingPipeline;
        this.uploadPath = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload folder on disk", e);
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentResponseDto> getDocumentsByWorkspace(Long workspaceId) {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new IllegalArgumentException("Workspace not found with ID: " + workspaceId);
        }
        return documentRepository.findByWorkspaceId(workspaceId).stream()
                .map(EntityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocumentResponseDto getDocumentById(Long id) {
        Document doc = findEntityById(id);
        return EntityMapper.toDto(doc);
    }

    public DocumentResponseDto uploadDocument(Long workspaceId, MultipartFile file) {
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

        // Build and persist the document entity
        Document doc = Document.builder()
                .workspace(workspace)
                .title(originalFilename)
                .originalFileName(storedFileName)
                .fileType(extension.toUpperCase())
                .size(file.getSize())
                .uploadTime(LocalDateTime.now())
                .processingStatus(ProcessingStatus.UPLOADING)
                .totalChunks(0)
                .build();

        Document savedDoc = documentRepository.save(doc);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            // Trigger asynchronous processing pipeline
            documentProcessingPipeline.processAsynchronously(savedDoc.getId(), targetLocation, extension.toUpperCase());
        } catch (IOException e) {
            savedDoc.setProcessingStatus(ProcessingStatus.FAILED);
            documentRepository.save(savedDoc);
            throw new RuntimeException("Failed to store or process file", e);
        }

        // Update workspace metadata stats
        workspace.setTotalDocuments(workspace.getTotalDocuments() + 1);
        workspaceRepository.save(workspace);

        return EntityMapper.toDto(savedDoc);
    }

    public void deleteDocument(Long id) {
        Document doc = findEntityById(id);

        // Delete local file from disk
        Path filePath = this.uploadPath.resolve(doc.getOriginalFileName());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Warning: could not delete disk file " + filePath + ": " + e.getMessage());
        }

        // Delete text chunks from in-memory store
        chunkingService.deleteChunksForDocument(id);

        // Soft-delete document (via @SQLDelete)
        documentRepository.delete(doc);

        // Update workspace stats
        Workspace workspace = doc.getWorkspace();
        if (workspace != null) {
            workspace.setTotalDocuments(Math.max(0, workspace.getTotalDocuments() - 1));
            workspaceRepository.save(workspace);
        }
    }

    /**
     * Internal helper: returns the raw entity for service-layer use.
     */
    public Document findEntityById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + id));
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastIndex + 1);
    }
}
