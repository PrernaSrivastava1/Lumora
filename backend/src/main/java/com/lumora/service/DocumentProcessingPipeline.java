package com.lumora.service;

import com.lumora.algorithms.common.vector.Vector;
import com.lumora.document.ParsedDocument;
import com.lumora.index.IndexManager;
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
public class DocumentProcessingPipeline {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingPipeline.class);

    private final DocumentRepository documentRepository;
    private final ProcessingService processingService;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final VectorEmbeddingRepository embeddingRepository;
    private final IndexManager indexManager;
    private final WorkspaceRepository workspaceRepository;

    public DocumentProcessingPipeline(DocumentRepository documentRepository,
                                      ProcessingService processingService,
                                      ChunkingService chunkingService,
                                      EmbeddingService embeddingService,
                                      VectorEmbeddingRepository embeddingRepository,
                                      IndexManager indexManager,
                                      WorkspaceRepository workspaceRepository) {
        this.documentRepository = documentRepository;
        this.processingService = processingService;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
        this.embeddingRepository = embeddingRepository;
        this.indexManager = indexManager;
        this.workspaceRepository = workspaceRepository;
    }

    @Async
    @Transactional
    public void processAsynchronously(Long documentId, Path filePath, String extension) {
        logger.info("Starting asynchronous processing pipeline for document ID: {}", documentId);

        Document doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null) {
            logger.error("Document not found with ID: {}, aborting processing.", documentId);
            return;
        }

        try {
            // Stage 1: PROCESSING (Text Extraction)
            updateStatus(doc, ProcessingStatus.PROCESSING);
            ParsedDocument parsed = processingService.processDocument(filePath, extension.toUpperCase());
            String text = parsed.getText();
            if (text == null || text.isBlank()) {
                throw new IllegalStateException("Extracted text is empty or blank.");
            }

            // Stage 2: CHUNKING
            updateStatus(doc, ProcessingStatus.CHUNKING);
            List<DocumentChunk> chunks = chunkingService.chunkAndStore(doc.getId(), text, null);
            doc.setTotalChunks(chunks.size());
            documentRepository.saveAndFlush(doc);

            // Stage 3 & 4: EMBEDDING & INDEXING
            updateStatus(doc, ProcessingStatus.EMBEDDING);
            VectorIndex index = indexManager.getOrCreateIndex(doc.getWorkspace().getId());
            String provider = embeddingService.getActiveProviderName();

            int newVectorsCount = 0;
            for (DocumentChunk chunk : chunks) {
                float[] floatEmbedding = embeddingService.generateEmbedding(chunk.getContent());
                
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

            // Update workspace total statistics
            Workspace workspace = doc.getWorkspace();
            workspace.setTotalVectors(workspace.getTotalVectors() + newVectorsCount);
            workspaceRepository.saveAndFlush(workspace);

            // Stage 5: READY
            updateStatus(doc, ProcessingStatus.READY);
            logger.info("Asynchronous processing pipeline completed successfully for document: {}", doc.getTitle());

        } catch (Exception e) {
            logger.error("Error occurred in document processing pipeline for ID: " + documentId, e);
            try {
                updateStatus(doc, ProcessingStatus.FAILED);
            } catch (Exception updateEx) {
                logger.error("Failed to update status to FAILED for document: " + documentId, updateEx);
            }
        }
    }

    private void updateStatus(Document doc, ProcessingStatus status) {
        doc.setProcessingStatus(status);
        documentRepository.saveAndFlush(doc);
    }
}
