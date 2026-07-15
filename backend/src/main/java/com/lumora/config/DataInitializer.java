package com.lumora.config;

import com.lumora.model.*;
import com.lumora.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final VectorEmbeddingRepository embeddingRepository;

    public DataInitializer(WorkspaceRepository workspaceRepository,
                           UserRepository userRepository,
                           DocumentRepository documentRepository,
                           ChunkRepository chunkRepository,
                           VectorEmbeddingRepository embeddingRepository) {
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.embeddingRepository = embeddingRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (workspaceRepository.count() > 0) {
            return;
        }

        // 1. Get or create developer user
        User owner = userRepository.findByUsername("developer")
                .orElseGet(() -> userRepository.saveAndFlush(User.builder()
                        .username("developer")
                        .email("dev@lumora.ai")
                        .password("password")
                        .roles(Set.of(Role.ROLE_USER))
                        .build()));

        // 2. Create demo workspace
        Workspace workspace = Workspace.builder()
                .name("Demo Workspace")
                .description("Preloaded sample workspace with resumes, tech specs, and project docs for instant testing.")
                .owner(owner)
                .totalDocuments(3)
                .totalVectors(5)
                .build();
        workspace = workspaceRepository.save(workspace);

        // 3. Document 1: John Doe Resume
        Document doc1 = Document.builder()
                .workspace(workspace)
                .title("john_doe_resume.pdf")
                .originalFileName("demo_john_doe_resume.pdf")
                .fileType("PDF")
                .size(12400)
                .uploadTime(LocalDateTime.now().minusDays(5))
                .processingStatus(ProcessingStatus.READY)
                .processingStart(LocalDateTime.now().minusDays(5))
                .processingEnd(LocalDateTime.now().minusDays(5).plusSeconds(2))
                .totalChunks(2)
                .build();
        doc1 = documentRepository.save(doc1);

        createChunkAndEmbedding(doc1, 0, 
                "John Doe - Senior Software Engineer. Contact: john.doe@email.com. Experience: 6 years building backend systems using Java, Spring Boot, PostgreSQL, and AWS. Designed microservices architecture reducing latency by 40%.",
                384, 0.1f);
        createChunkAndEmbedding(doc1, 1,
                "Education: BS in Computer Science from MIT. Skills: Java, Spring Boot, REST APIs, Hibernate, PostgreSQL, Docker, Kubernetes, AWS. Certified AWS Solutions Architect.",
                384, 0.2f);

        // 4. Document 2: Jane Smith Resume
        Document doc2 = Document.builder()
                .workspace(workspace)
                .title("jane_smith_resume.docx")
                .originalFileName("demo_jane_smith_resume.docx")
                .fileType("DOCX")
                .size(15200)
                .uploadTime(LocalDateTime.now().minusDays(3))
                .processingStatus(ProcessingStatus.READY)
                .processingStart(LocalDateTime.now().minusDays(3))
                .processingEnd(LocalDateTime.now().minusDays(3).plusSeconds(3))
                .totalChunks(2)
                .build();
        doc2 = documentRepository.save(doc2);

        createChunkAndEmbedding(doc2, 0,
                "Jane Smith - Full Stack Developer. Contact: jane.smith@email.com. Experience: 5 years crafting user interfaces and APIs with React, TypeScript, Node.js, Next.js, and MongoDB. Led development of a dashboard used by 10k daily active users.",
                384, 0.3f);
        createChunkAndEmbedding(doc2, 1,
                "Education: MS in Software Engineering from Stanford. Skills: React, TypeScript, JavaScript, CSS, HTML, Vite, TailwindCSS, Node.js, Express, MongoDB. Passionate about responsive UI design.",
                384, 0.4f);

        // 5. Document 3: System Architecture
        Document doc3 = Document.builder()
                .workspace(workspace)
                .title("system_architecture.txt")
                .originalFileName("demo_system_architecture.txt")
                .fileType("TXT")
                .size(8500)
                .uploadTime(LocalDateTime.now().minusDays(1))
                .processingStatus(ProcessingStatus.READY)
                .processingStart(LocalDateTime.now().minusDays(1))
                .processingEnd(LocalDateTime.now().minusDays(1).plusSeconds(1))
                .totalChunks(1)
                .build();
        doc3 = documentRepository.save(doc3);

        createChunkAndEmbedding(doc3, 0,
                "System Architecture Document: The Lumora application runs with a Spring Boot backend and React frontend. It indexes uploaded files by chunking them semantically and storing their vector embeddings. The Vector Store supports HNSW, KD-Tree, and Brute Force search strategies.",
                384, 0.5f);
    }

    private void createChunkAndEmbedding(Document doc, int index, String content, int dimensions, float valOffset) {
        DocumentChunk chunk = DocumentChunk.builder()
                .document(doc)
                .chunkIndex(index)
                .content(content)
                .tokenCount(content.split("\\s+").length)
                .startChar(0)
                .endChar(content.length())
                .build();
        chunk = chunkRepository.save(chunk);

        float[] vector = new float[dimensions];
        for (int i = 0; i < dimensions; i++) {
            vector[i] = (float) Math.sin(i * 0.1 + valOffset);
        }

        VectorEmbedding embedding = VectorEmbedding.builder()
                .chunk(chunk)
                .dimensions(dimensions)
                .modelName("nomic-embed-text")
                .build();
        embedding.setVectorFromFloats(vector);
        embeddingRepository.save(embedding);
    }
}
