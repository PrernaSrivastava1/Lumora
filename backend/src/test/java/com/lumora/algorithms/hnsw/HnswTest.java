package com.lumora.algorithms.hnsw;

import com.lumora.algorithms.common.vector.DistanceCalculator;
import com.lumora.algorithms.common.vector.EuclideanDistanceCalculator;
import com.lumora.algorithms.common.vector.Vector;
import com.lumora.model.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HnswTest {

    @Test
    void testHnswIndexBuildAndSearch() {
        HnswIndex index = new HnswIndex();
        DistanceCalculator calculator = new EuclideanDistanceCalculator();

        List<Vector> vectors = new ArrayList<>();
        vectors.add(new Vector(1L, new float[]{1.0f, 1.0f}, 101L));
        vectors.add(new Vector(2L, new float[]{2.0f, 2.0f}, 102L));
        vectors.add(new Vector(3L, new float[]{3.0f, 3.0f}, 103L));
        vectors.add(new Vector(4L, new float[]{4.0f, 4.0f}, 104L));

        index.build(vectors, calculator);

        assertNotNull(index.getEnterPoint());
        assertTrue(index.getMaxLevel() >= 0);

        List<SearchResult> hits = index.search(new float[]{2.1f, 1.9f}, 2, calculator);

        assertEquals(2, hits.size());
        // Closest should be V2 [2, 2]
        assertEquals(2L, hits.get(0).getChunkId());
    }

    @Test
    void testDynamicInsert() {
        HnswIndex index = new HnswIndex();
        DistanceCalculator calculator = new EuclideanDistanceCalculator();

        Vector v1 = new Vector(1L, new float[]{0.1f, 0.2f}, 101L);
        Vector v2 = new Vector(2L, new float[]{0.9f, 0.8f}, 102L);

        index.insert(v1, calculator);
        index.insert(v2, calculator);

        assertEquals(2, index.count());

        List<SearchResult> hits = index.search(new float[]{0.12f, 0.18f}, 1, calculator);
        assertEquals(1, hits.size());
        assertEquals(1L, hits.get(0).getChunkId());
    }

    @Test
    void testDynamicDelete() {
        HnswIndex index = new HnswIndex();
        DistanceCalculator calculator = new EuclideanDistanceCalculator();

        Vector v1 = new Vector(1L, new float[]{1.0f, 1.0f}, 101L);
        Vector v2 = new Vector(2L, new float[]{2.0f, 2.0f}, 102L);
        Vector v3 = new Vector(3L, new float[]{3.0f, 3.0f}, 103L);

        index.insert(v1, calculator);
        index.insert(v2, calculator);
        index.insert(v3, calculator);

        assertEquals(3, index.count());

        // Delete v2
        index.delete(v2, calculator);
        assertEquals(2, index.count());

        List<SearchResult> hits = index.search(new float[]{2.0f, 2.0f}, 1, calculator);
        assertEquals(1, hits.size());
        // Closest should now be v1 or v3 since v2 is deleted
        long bestId = hits.get(0).getChunkId();
        assertTrue(bestId == 1L || bestId == 3L);
    }
}
