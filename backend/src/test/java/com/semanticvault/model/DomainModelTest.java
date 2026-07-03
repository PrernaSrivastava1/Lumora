package com.semanticvault.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DomainModelTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testWorkspaceBuilderAndValidation() {
        LocalDateTime now = LocalDateTime.now();
        Workspace workspace = Workspace.builder()
                .id(1L)
                .name("Default Space")
                .description("Test Description")
                .totalDocuments(5)
                .totalVectors(100)
                .build();
        workspace.setCreatedAt(now);
        workspace.setUpdatedAt(now);

        assertEquals(1L, workspace.getId());
        assertEquals("Default Space", workspace.getName());
        assertEquals(5, workspace.getTotalDocuments());
        assertEquals(100, workspace.getTotalVectors());

        Set<ConstraintViolation<Workspace>> violations = validator.validate(workspace);
        assertTrue(violations.isEmpty(), "Valid workspace should have no violations");

        // Test validation constraint violation
        Workspace invalidWorkspace = Workspace.builder()
                .name("") // Blank name violation
                .totalDocuments(-1) // Negative documents violation
                .build();
        invalidWorkspace.setCreatedAt(null); // Null date violation

        Set<ConstraintViolation<Workspace>> badViolations = validator.validate(invalidWorkspace);
        assertFalse(badViolations.isEmpty());
        assertEquals(3, badViolations.size());
    }

    @Test
    void testDocumentBuilderAndValidation() {
        LocalDateTime now = LocalDateTime.now();
        Document doc = Document.builder()
                .id(10L)
                .workspace(Workspace.builder().id(1L).build())
                .title("LLM Architectures")
                .originalFileName("llm_arch.pdf")
                .fileType("pdf")
                .size(1024L)
                .uploadTime(now)
                .processingStatus(ProcessingStatus.READY)
                .totalChunks(4)
                .build();

        assertEquals("llm_arch.pdf", doc.getOriginalFileName());
        assertEquals(ProcessingStatus.READY, doc.getProcessingStatus());

        Set<ConstraintViolation<Document>> violations = validator.validate(doc);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testVectorEmbeddingBuilderAndValidation() {
        float[] vector = new float[]{0.1f, 0.2f, 0.3f};
        VectorEmbedding emb = VectorEmbedding.builder()
                .id(100L)
                .modelName("nomic-embed-text")
                .dimensions(3)
                .build();
        emb.setVectorFromFloats(vector);
        emb.setCreatedAt(LocalDateTime.now());

        assertArrayEquals(vector, emb.getVectorAsFloats());
        assertEquals(3, emb.getDimensions());

        Set<ConstraintViolation<VectorEmbedding>> violations = validator.validate(emb);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testToStringAndEqualsHashCode() {
        Workspace w1 = Workspace.builder().id(1L).name("A").build();
        w1.setCreatedAt(LocalDateTime.MIN);
        Workspace w2 = Workspace.builder().id(1L).name("A").build();
        w2.setCreatedAt(LocalDateTime.MIN);
        Workspace w3 = Workspace.builder().id(2L).name("B").build();
        w3.setCreatedAt(LocalDateTime.MIN);

        assertEquals(w1, w2);
        assertNotEquals(w1, w3);
        assertEquals(w1.hashCode(), w2.hashCode());
        assertNotNull(w1.toString());
    }
}
