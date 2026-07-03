package com.lumora.rag.chunking;

import com.lumora.model.Document;
import com.lumora.model.DocumentChunk;
import com.lumora.model.Workspace;
import com.lumora.repository.DocumentRepository;
import com.lumora.repository.WorkspaceRepository;
import com.lumora.service.ChunkingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ChunkingStrategyTest {

    @Autowired
    private ChunkingService chunkingService;

    @Autowired
    private FixedSizeChunker fixedSizeChunker;

    @Autowired
    private SlidingWindowChunker slidingWindowChunker;

    @Autowired
    private SemanticChunker semanticChunker;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    void testFixedSizeChunkerSplitting() {
        FixedSizeChunker chunker = new FixedSizeChunker(10); // 10 chars per chunk
        String text = "abcdefghijklmnopqrstuvwxyz"; // 26 chars

        List<DocumentChunk> chunks = chunker.chunk(text, 1L);
        assertEquals(3, chunks.size());
        
        assertEquals("abcdefghij", chunks.get(0).getContent());
        assertEquals(0, chunks.get(0).getStartChar());
        assertEquals(10, chunks.get(0).getEndChar());

        assertEquals("klmnopqrst", chunks.get(1).getContent());
        assertEquals(10, chunks.get(1).getStartChar());
        assertEquals(20, chunks.get(1).getEndChar());

        assertEquals("uvwxyz", chunks.get(2).getContent());
        assertEquals(20, chunks.get(2).getStartChar());
        assertEquals(26, chunks.get(2).getEndChar());
    }

    @Test
    void testSlidingWindowChunkerOverlapping() {
        SlidingWindowChunker chunker = new SlidingWindowChunker(10, 4); // 10 size, 4 overlap
        String text = "abcdefghijklmnop"; // 16 chars
        // Chunk 0: indices 0-10 -> "abcdefghij"
        // Chunk 1: step = 10-4=6. Start index = 6. end = 16 -> "ghijklmnop"

        List<DocumentChunk> chunks = chunker.chunk(text, 1L);
        assertEquals(2, chunks.size());

        assertEquals("abcdefghij", chunks.get(0).getContent());
        assertEquals(0, chunks.get(0).getStartChar());
        assertEquals(10, chunks.get(0).getEndChar());

        assertEquals("ghijklmnop", chunks.get(1).getContent());
        assertEquals(6, chunks.get(1).getStartChar());
        assertEquals(16, chunks.get(1).getEndChar());
    }

    @Test
    void testSemanticChunkerSentenceGrouping() {
        String text = "First sentence. Second sentence! Third sentence? Fourth sentence.";
        List<DocumentChunk> chunks = semanticChunker.chunk(text, 1L);
        
        // Groups 3 sentences together
        // Chunk 0: "First sentence. Second sentence! Third sentence?"
        // Chunk 1: " Fourth sentence."
        assertEquals(2, chunks.size());
        assertTrue(chunks.get(0).getContent().contains("Third sentence?"));
        assertTrue(!chunks.get(0).getContent().contains("Fourth sentence."));
        assertTrue(chunks.get(1).getContent().contains("Fourth sentence."));
    }

    @Test
    void testChunkingServiceIntegration() {
        Workspace ws = Workspace.builder().name("Test Chunking WS").build();
        ws = workspaceRepository.save(ws);

        Document doc = Document.builder()
                .workspace(ws)
                .title("Test Doc")
                .originalFileName("test.txt")
                .processingStatus(com.lumora.model.ProcessingStatus.READY)
                .build();
        doc = documentRepository.save(doc);
        Long docId = doc.getId();

        String text = "Orchestrated chunking pipeline logic.";
        // Run FIXED_SIZE chunking
        List<DocumentChunk> chunks = chunkingService.chunkAndStore(docId, text, "FIXED_SIZE");
        assertTrue(chunks.size() > 0);
        
        List<DocumentChunk> retrieved = chunkingService.getChunksForDocument(docId);
        assertEquals(chunks.size(), retrieved.size());

        chunkingService.deleteChunksForDocument(docId);
        assertTrue(chunkingService.getChunksForDocument(docId).isEmpty());
    }
}
