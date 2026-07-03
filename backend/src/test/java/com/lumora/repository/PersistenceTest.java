package com.lumora.repository;

import com.lumora.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PersistenceTest {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private VectorEmbeddingRepository vectorEmbeddingRepository;

    @Test
    void testWorkspaceCRUD() {
        Workspace ws = Workspace.builder()
                .name("Test Workspace")
                .description("A test workspace")
                .build();
        Workspace saved = workspaceRepository.save(ws);
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals("Test Workspace", saved.getName());

        // Read
        Workspace found = workspaceRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("A test workspace", found.getDescription());

        // Update
        found.setName("Updated Workspace");
        workspaceRepository.save(found);
        Workspace updated = workspaceRepository.findById(found.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals("Updated Workspace", updated.getName());
    }

    @Test
    void testSoftDelete() {
        Workspace ws = Workspace.builder()
                .name("Soft Delete Test")
                .description("Will be soft-deleted")
                .build();
        Workspace saved = workspaceRepository.save(ws);
        Long id = saved.getId();

        // Delete (soft)
        workspaceRepository.deleteById(id);

        // Should not be found via JPA (filtered by @SQLRestriction)
        assertTrue(workspaceRepository.findById(id).isEmpty());
    }

    @Test
    void testDocumentWorkspaceRelationship() {
        Workspace ws = Workspace.builder()
                .name("Doc Workspace")
                .build();
        ws = workspaceRepository.save(ws);

        Document doc = Document.builder()
                .workspace(ws)
                .title("test.pdf")
                .originalFileName("123_test.pdf")
                .fileType("PDF")
                .size(1024)
                .uploadTime(LocalDateTime.now())
                .processingStatus(ProcessingStatus.READY)
                .build();
        Document savedDoc = documentRepository.save(doc);

        assertNotNull(savedDoc.getId());
        assertEquals(ws.getId(), savedDoc.getWorkspace().getId());

        List<Document> docs = documentRepository.findByWorkspaceId(ws.getId());
        assertEquals(1, docs.size());
    }

    @Test
    void testChunkDocumentRelationship() {
        Workspace ws = workspaceRepository.save(Workspace.builder().name("Chunk WS").build());
        Document doc = documentRepository.save(Document.builder()
                .workspace(ws)
                .title("chunk-test.txt")
                .originalFileName("chunk-test.txt")
                .fileType("TXT")
                .uploadTime(LocalDateTime.now())
                .processingStatus(ProcessingStatus.READY)
                .build());

        DocumentChunk chunk = DocumentChunk.builder()
                .document(doc)
                .chunkIndex(0)
                .content("Hello chunk world")
                .tokenCount(3)
                .startChar(0)
                .endChar(17)
                .build();
        DocumentChunk savedChunk = chunkRepository.save(chunk);

        assertNotNull(savedChunk.getId());
        assertEquals(doc.getId(), savedChunk.getDocument().getId());

        List<DocumentChunk> chunks = chunkRepository.findByDocumentId(doc.getId());
        assertEquals(1, chunks.size());
        assertEquals("Hello chunk world", chunks.get(0).getContent());
    }

    @Test
    void testVectorEmbeddingChunkRelationship() {
        Workspace ws = workspaceRepository.save(Workspace.builder().name("Embed WS").build());
        Document doc = documentRepository.save(Document.builder()
                .workspace(ws)
                .title("embed-test.txt")
                .originalFileName("embed-test.txt")
                .fileType("TXT")
                .uploadTime(LocalDateTime.now())
                .processingStatus(ProcessingStatus.READY)
                .build());
        DocumentChunk chunk = chunkRepository.save(DocumentChunk.builder()
                .document(doc)
                .chunkIndex(0)
                .content("Embedding test content")
                .tokenCount(3)
                .build());

        VectorEmbedding embedding = VectorEmbedding.builder()
                .chunk(chunk)
                .modelName("nomic-embed-text")
                .dimensions(3)
                .build();
        embedding.setVectorFromFloats(new float[]{0.1f, 0.2f, 0.3f});

        VectorEmbedding saved = vectorEmbeddingRepository.save(embedding);
        assertNotNull(saved.getId());

        float[] values = saved.getVectorAsFloats();
        assertEquals(3, values.length);
        assertEquals(0.1f, values[0], 1e-5);
        assertEquals(0.2f, values[1], 1e-5);
        assertEquals(0.3f, values[2], 1e-5);
    }

    @Test
    void testAutoAuditTimestamps() {
        Workspace ws = Workspace.builder().name("Audit Test").build();
        Workspace saved = workspaceRepository.save(ws);
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertFalse(saved.isDeleted());
    }
}
