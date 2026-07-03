package com.lumora.index;

import com.lumora.algorithms.common.vector.Vector;
import com.lumora.model.Document;
import com.lumora.model.DocumentChunk;
import com.lumora.model.IndexStats;
import com.lumora.model.VectorEmbedding;
import com.lumora.model.Workspace;
import com.lumora.repository.ChunkRepository;
import com.lumora.repository.DocumentRepository;
import com.lumora.repository.VectorEmbeddingRepository;
import com.lumora.repository.VectorStore;
import com.lumora.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VectorStoreAndIndexManagerTest {

    @Autowired
    private IndexManager indexManager;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private VectorEmbeddingRepository embeddingRepository;

    @BeforeEach
    void setUp() {
        indexManager.clearAll();
    }

    @Test
    void testVectorIndexCRUDAndStats() {
        VectorIndex index = new VectorIndex(10L);
        assertEquals(10L, index.getWorkspaceId());
        assertEquals(0, index.count());

        Vector v1 = new Vector(101L, new float[]{0.1f, 0.2f}, 1001L);
        Vector v2 = new Vector(102L, new float[]{0.3f, 0.4f}, 1002L);

        index.add(v1);
        index.add(v2);

        assertEquals(2, index.count());
        assertEquals(v1, index.get(101L));
        assertEquals(v2, index.get(102L));

        IndexStats stats = index.getStats();
        assertEquals(10L, stats.getWorkspaceId());
        assertEquals(2, stats.getCount());
        assertEquals(2, stats.getDimension());
        assertTrue(stats.getMemoryUsageBytes() > 0);
        assertNotNull(stats.getLastUpdatedAt());

        index.remove(101L);
        assertEquals(1, index.count());
        assertNull(index.get(101L));

        index.clear();
        assertEquals(0, index.count());
        assertEquals(0, index.getStats().getDimension());
    }

    @Test
    void testIndexManagerMultiWorkspaceHandling() {
        assertFalse(indexManager.hasIndex(1L));
        assertFalse(indexManager.hasIndex(2L));

        VectorIndex index1 = indexManager.getOrCreateIndex(1L);
        VectorIndex index2 = indexManager.getOrCreateIndex(2L);

        assertTrue(indexManager.hasIndex(1L));
        assertTrue(indexManager.hasIndex(2L));

        index1.add(new Vector(101L, new float[]{0.5f, 0.5f}, 1001L));
        index2.add(new Vector(201L, new float[]{0.6f, 0.6f, 0.6f}, 2001L));

        assertEquals(1, indexManager.getIndex(1L).count());
        assertEquals(1, indexManager.getIndex(2L).count());

        IndexStats stats1 = indexManager.getIndexStats(1L);
        IndexStats stats2 = indexManager.getIndexStats(2L);

        assertEquals(2, stats1.getDimension());
        assertEquals(3, stats2.getDimension());

        Map<Long, IndexStats> allStats = indexManager.getAllIndexStats();
        assertEquals(2, allStats.size());
        assertTrue(allStats.containsKey(1L));
        assertTrue(allStats.containsKey(2L));

        indexManager.unloadWorkspaceVectors(1L);
        assertFalse(indexManager.hasIndex(1L));
        assertTrue(indexManager.hasIndex(2L));

        indexManager.clearAll();
        assertFalse(indexManager.hasIndex(2L));
    }

    @Test
    void testVectorIndexThreadSafety() throws Exception {
        VectorIndex index = new VectorIndex(1L);
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            futures.add(executor.submit(() -> {
                try {
                    latch.await();
                    for (int j = 0; j < operationsPerThread; j++) {
                        long id = threadId * 1000L + j;
                        index.add(new Vector(id, new float[]{0.1f}, id));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        latch.countDown();
        for (Future<?> future : futures) {
            future.get();
        }
        executor.shutdown();

        assertEquals(threadCount * operationsPerThread, index.count());
        IndexStats stats = index.getStats();
        assertEquals(threadCount * operationsPerThread, stats.getCount());
        assertEquals(1, stats.getDimension());
    }

    @Test
    @Transactional
    void testLoadWorkspaceVectorsFromDatabase() {
        // Prepare DB entities
        Workspace ws = Workspace.builder().name("Test WS").build();
        ws = workspaceRepository.save(ws);

        Document doc = Document.builder()
                .workspace(ws)
                .title("Test Doc")
                .originalFileName("test.txt")
                .processingStatus(com.lumora.model.ProcessingStatus.READY)
                .build();
        doc = documentRepository.save(doc);

        DocumentChunk chunk1 = DocumentChunk.builder()
                .document(doc)
                .chunkIndex(0)
                .content("Hello world")
                .build();
        chunk1 = chunkRepository.save(chunk1);

        DocumentChunk chunk2 = DocumentChunk.builder()
                .document(doc)
                .chunkIndex(1)
                .content("Semantic vault rules")
                .build();
        chunk2 = chunkRepository.save(chunk2);

        VectorEmbedding ve1 = VectorEmbedding.builder()
                .chunk(chunk1)
                .modelName("MOCK")
                .dimensions(3)
                .build();
        ve1.setVectorFromFloats(new float[]{1.0f, 2.0f, 3.0f});
        ve1 = embeddingRepository.save(ve1);

        VectorEmbedding ve2 = VectorEmbedding.builder()
                .chunk(chunk2)
                .modelName("MOCK")
                .dimensions(3)
                .build();
        ve2.setVectorFromFloats(new float[]{4.0f, 5.0f, 6.0f});
        ve2 = embeddingRepository.save(ve2);

        // Load into memory index
        indexManager.loadWorkspaceVectors(ws.getId());

        assertTrue(indexManager.hasIndex(ws.getId()));
        VectorIndex index = indexManager.getIndex(ws.getId());
        assertEquals(2, index.count());

        Vector v1 = index.get(chunk1.getId());
        assertNotNull(v1);
        assertArrayEquals(new float[]{1.0f, 2.0f, 3.0f}, v1.getValues());

        Vector v2 = index.get(chunk2.getId());
        assertNotNull(v2);
        assertArrayEquals(new float[]{4.0f, 5.0f, 6.0f}, v2.getValues());
    }
}
