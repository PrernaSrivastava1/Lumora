package com.lumora.algorithms.kdtree;

import com.lumora.algorithms.common.vector.CosineDistanceCalculator;
import com.lumora.algorithms.common.vector.DistanceCalculator;
import com.lumora.algorithms.common.vector.DistanceCalculatorFactory;
import com.lumora.algorithms.common.vector.EuclideanDistanceCalculator;
import com.lumora.algorithms.common.vector.Vector;
import com.lumora.model.DistanceMetric;
import com.lumora.model.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KdTreeTest {

    @Test
    void testKdTreeBuildAndKnnSearch() {
        KdTree tree = new KdTree();
        
        List<Vector> vectors = new ArrayList<>();
        vectors.add(new Vector(1L, new float[]{1.0f, 2.0f}, 101L));
        vectors.add(new Vector(2L, new float[]{3.0f, 4.0f}, 102L));
        vectors.add(new Vector(3L, new float[]{5.0f, 6.0f}, 103L));
        vectors.add(new Vector(4L, new float[]{7.0f, 8.0f}, 104L));

        tree.build(vectors);
        assertNotNull(tree.getRoot());

        DistanceCalculator calculator = new EuclideanDistanceCalculator();
        float[] query = new float[]{5.2f, 5.8f};

        List<SearchResult> hits = tree.knnSearch(query, 2, calculator, DistanceMetric.EUCLIDEAN);

        assertEquals(2, hits.size());
        // Rank 1 should be V3 [5, 6]
        assertEquals(3L, hits.get(0).getChunkId());
        // Rank 2 should be V2 [3, 4] or V4 [7, 8]
        assertTrue(hits.get(1).getChunkId() == 2L || hits.get(1).getChunkId() == 4L);
    }

    @Test
    void testDynamicInsert() {
        KdTree tree = new KdTree();
        tree.insert(new Vector(1L, new float[]{1.0f, 1.0f}, 101L));
        tree.insert(new Vector(2L, new float[]{2.0f, 2.0f}, 102L));

        assertNotNull(tree.getRoot());
        DistanceCalculator calculator = new EuclideanDistanceCalculator();
        
        List<SearchResult> hits = tree.knnSearch(new float[]{1.1f, 0.9f}, 1, calculator, DistanceMetric.EUCLIDEAN);
        assertEquals(1, hits.size());
        assertEquals(1L, hits.get(0).getChunkId());
    }

    @Test
    void testDynamicDelete() {
        KdTree tree = new KdTree();
        Vector v1 = new Vector(1L, new float[]{1.0f, 1.0f}, 101L);
        Vector v2 = new Vector(2L, new float[]{2.0f, 2.0f}, 102L);
        Vector v3 = new Vector(3L, new float[]{3.0f, 3.0f}, 103L);

        tree.insert(v1);
        tree.insert(v2);
        tree.insert(v3);

        // Delete root v1
        tree.delete(v1);

        DistanceCalculator calculator = new EuclideanDistanceCalculator();
        List<SearchResult> hits = tree.knnSearch(new float[]{1.0f, 1.0f}, 1, calculator, DistanceMetric.EUCLIDEAN);

        assertEquals(1, hits.size());
        assertEquals(2L, hits.get(0).getChunkId()); // Closest should now be v2 since v1 is deleted
    }

    @Test
    void testMatchesBruteForceOutput() {
        List<Vector> vectors = new ArrayList<>();
        vectors.add(new Vector(1L, new float[]{0.15f, 0.22f, 0.88f}, 101L));
        vectors.add(new Vector(2L, new float[]{0.45f, 0.12f, 0.38f}, 102L));
        vectors.add(new Vector(3L, new float[]{0.05f, 0.92f, 0.11f}, 103L));
        vectors.add(new Vector(4L, new float[]{0.85f, 0.32f, 0.05f}, 104L));

        KdTree tree = new KdTree();
        tree.build(vectors);

        float[] query = new float[]{0.1f, 0.2f, 0.9f};
        DistanceCalculator calculator = new CosineDistanceCalculator();

        List<SearchResult> kdHits = tree.knnSearch(query, 2, calculator, DistanceMetric.COSINE);

        // Brute force exact scan
        List<SearchResult> bfHits = vectors.stream()
                .map(v -> SearchResult.builder()
                        .chunkId(v.getId())
                        .score(calculator.calculate(new Vector(null, query, null), v))
                        .build())
                .sorted((a, b) -> Double.compare(a.getScore(), b.getScore()))
                .limit(2)
                .toList();

        assertEquals(bfHits.size(), kdHits.size());
        for (int i = 0; i < bfHits.size(); i++) {
            assertEquals(bfHits.get(i).getChunkId(), kdHits.get(i).getChunkId());
            assertEquals(bfHits.get(i).getScore(), kdHits.get(i).getScore(), 1e-6);
        }
    }
}
