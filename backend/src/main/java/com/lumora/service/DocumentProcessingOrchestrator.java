package com.lumora.service;

import com.lumora.algorithms.common.vector.Vector;
import com.lumora.index.VectorIndex;
import com.lumora.model.*;
import com.lumora.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;

@Service
public class DocumentProcessingOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingOrchestrator.class);

    private final DocumentRepository documentRepository;
    private final TextExtractionService textExtractionService;
    private final TextCleaningService textCleaningService;
    private final LanguageDetectionService languageDetectionService;
    private final SemanticChunkingService semanticChunkingService;
    private final EmbeddingGenerationService embeddingGenerationService;
    private final VectorEmbeddingRepository embeddingRepository;
    private final VectorIndexService vectorIndexService;
    private final WorkspaceRepository workspaceRepository;
    private final ProcessingStatusService processingStatusService;

    public DocumentProcessingOrchestrator(DocumentRepository documentRepository,
                                          TextExtractionService textExtractionService,
                                          TextCleaningService textCleaningService,
                                          LanguageDetectionService languageDetectionService,
                                          SemanticChunkingService semanticChunkingService,
                                          EmbeddingGenerationService embeddingGenerationService,
                                          VectorEmbeddingRepository embeddingRepository,
                                          VectorIndexService vectorIndexService,
                                          WorkspaceRepository workspaceRepository,
                                          ProcessingStatusService processingStatusService) {
        this.documentRepository = documentRepository;
        this.textExtractionService = textExtractionService;
        this.textCleaningService = textCleaningService;
        this.languageDetectionService = languageDetectionService;
        this.semanticChunkingService = semanticChunkingService;
        this.embeddingGenerationService = embeddingGenerationService;
        this.embeddingRepository = embeddingRepository;
        this.vectorIndexService = vectorIndexService;
        this.workspaceRepository = workspaceRepository;
        this.processingStatusService = processingStatusService;
    }

    @Async
    @Transactional
    public void processAsynchronously(Long documentId, Path filePath, String extension) {
        logger.info("Starting intelligent document pipeline for ID: {}", documentId);
        
        try {
            // Stage 1: VALIDATING
            processingStatusService.updateStatus(documentId, ProcessingStatus.VALIDATING);
            String extUpper = extension.toUpperCase();
            if (!extUpper.equals("PDF") && !extUpper.equals("DOCX") && !extUpper.equals("TXT") && !extUpper.equals("MD")) {
                throw new IllegalArgumentException("Unsupported file type: " + extension);
            }

            // Stage 2: EXTRACTING_TEXT
            processingStatusService.updateStatus(documentId, ProcessingStatus.EXTRACTING_TEXT);
            String rawText = textExtractionService.extractText(filePath, extension);
            if (rawText == null || rawText.isBlank()) {
                throw new IllegalStateException("Extraction failed: file is empty or unreadable.");
            }

            // Stage 3: CLEANING_TEXT
            processingStatusService.updateStatus(documentId, ProcessingStatus.CLEANING_TEXT);
            String cleanedText = textCleaningService.cleanText(rawText);

            // Stage 4: Detect Language (logged internally)
            String language = languageDetectionService.detectLanguage(cleanedText);
            logger.info("Detected language: {} for document ID: {}", language, documentId);

            // Stage 5: CHUNKING
            processingStatusService.updateStatus(documentId, ProcessingStatus.CHUNKING);
            List<DocumentChunk> chunks = semanticChunkingService.chunkText(documentId, cleanedText);
            
            Document doc = documentRepository.findById(documentId).orElseThrow();
            doc.setTotalChunks(chunks.size());
            documentRepository.saveAndFlush(doc);

            // Stage 6: GENERATING_EMBEDDINGS
            processingStatusService.updateStatus(documentId, ProcessingStatus.GENERATING_EMBEDDINGS);
            
            // Stage 7: INDEXING
            processingStatusService.updateStatus(documentId, ProcessingStatus.INDEXING);
            VectorIndex index = vectorIndexService.getOrCreateIndex(doc.getWorkspace().getId());
            String provider = embeddingGenerationService.getActiveModelName();

            int newVectorsCount = 0;
            for (DocumentChunk chunk : chunks) {
                float[] floatEmbedding = embeddingGenerationService.generateEmbedding(chunk.getContent());
                
                // Store embedding
                VectorEmbedding vecEmbedding = VectorEmbedding.builder()
                        .chunk(chunk)
                        .dimensions(floatEmbedding.length)
                        .modelName(provider)
                        .build();
                vecEmbedding.setVectorFromFloats(floatEmbedding);
                vecEmbedding = embeddingRepository.saveAndFlush(vecEmbedding);

                // Update HNSW Index
                Vector vector = new Vector(
                        chunk.getId(),
                        floatEmbedding,
                        vecEmbedding.getId()
                );
                index.add(vector);
                newVectorsCount++;
            }

            // Update workspace statistics
            Workspace workspace = doc.getWorkspace();
            workspace.setTotalVectors(workspace.getTotalVectors() + newVectorsCount);
            workspaceRepository.saveAndFlush(workspace);

            // Stage 8: READY
            processingStatusService.updateStatus(documentId, ProcessingStatus.READY);
            logger.info("Intelligent document pipeline completed successfully for document: {}", doc.getTitle());

        } catch (Exception e) {
            logger.error("Error occurred in document pipeline for ID: " + documentId, e);
            processingStatusService.logFailure(documentId, e.getMessage());
        }
    }

    @Async
    @Transactional
    public void retry(Long documentId) {
        Document doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null || doc.getProcessingStatus() != ProcessingStatus.FAILED) {
            logger.warn("Document is not in FAILED status, aborting retry for ID: {}", documentId);
            return;
        }
        
        // Find stored file path
        String uploadDir = "uploads/";
        String safeName = doc.getTitle();
        // Fall back to original file name or find in uploads
        Path path = Path.of(uploadDir, doc.getOriginalFileName()).toAbsolutePath();
        if (!path.toFile().exists()) {
            // Find any matching file in uploads directory
            Path uploadsDir = Path.of("uploads");
            try {
                Path matched = java.nio.file.Files.list(uploadsDir)
                        .filter(p -> p.getFileName().toString().contains(doc.getOriginalFileName()))
                        .findFirst()
                        .orElse(null);
                if (matched != null) {
                    path = matched;
                }
            } catch (Exception ex) {
                logger.error("Error searching uploads dir", ex);
            }
        }
        
        processAsynchronously(documentId, path, doc.getFileType() != null ? doc.getFileType() : "TXT");
    }
}
