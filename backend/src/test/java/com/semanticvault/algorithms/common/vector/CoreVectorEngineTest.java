package com.semanticvault.algorithms.common.vector;

import com.semanticvault.algorithms.common.SearchException;
import com.semanticvault.model.DistanceMetric;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoreVectorEngineTest {

    @Test
    void testVectorImmutability() {
        float[] rawInput = new float[]{1.0f, 2.0f, 3.0f};
        Vector vector = new Vector(1L, rawInput, 100L);

        // Modifying input array should not modify Vector values (due to copying in constructor)
        rawInput[0] = 99.0f;
        assertNotEquals(99.0f, vector.getValues()[0]);
        assertEquals(1.0f, vector.getValues()[0]);

        // Modifying array returned by getter should not modify Vector internal state
        float[] returnedValues = vector.getValues();
        returnedValues[1] = 99.0f;
        assertEquals(2.0f, vector.getValues()[1]);
    }

    @Test
    void testVectorValidatorThrowsSearchException() {
        // Test null array
        assertThrows(SearchException.class, () -> VectorValidator.validate(null));

        // Test empty array
        assertThrows(SearchException.class, () -> VectorValidator.validate(new float[]{}));

        // Test NaN
        assertThrows(SearchException.class, () -> VectorValidator.validate(new float[]{1.0f, Float.NaN}));

        // Test Infinity
        assertThrows(SearchException.class, () -> VectorValidator.validate(new float[]{Float.POSITIVE_INFINITY, 2.0f}));

        // Test dimension mismatch
        Vector v = new Vector(1L, new float[]{1.0f, 2.0f}, 100L);
        assertThrows(SearchException.class, () -> VectorValidator.validateDimension(v.getValues(), 3));
    }

    @Test
    void testVectorMathematics() {
        Vector v1 = new Vector(1L, new float[]{1.0f, 0.0f}, 100L);
        Vector v2 = new Vector(2L, new float[]{0.0f, 1.0f}, 101L);

        // Magnitude
        assertEquals(1.0, v1.magnitude(), 1e-9);

        // Dot product
        assertEquals(0.0, VectorUtils.dotProduct(v1.getValues(), v2.getValues()), 1e-9);

        // Euclidean Distance: sqrt((1-0)^2 + (0-1)^2) = sqrt(2)
        DistanceCalculator euclidean = DistanceCalculatorFactory.getCalculator(DistanceMetric.EUCLIDEAN);
        assertEquals(Math.sqrt(2.0), euclidean.calculate(v1, v2), 1e-9);

        // Cosine Distance: 1.0 - (dot / (mag1 * mag2)) = 1.0 - 0 = 1.0
        DistanceCalculator cosine = DistanceCalculatorFactory.getCalculator(DistanceMetric.COSINE);
        assertEquals(1.0, cosine.calculate(v1, v2), 1e-9);

        // Manhattan Distance: |1-0| + |0-1| = 2.0
        DistanceCalculator manhattan = DistanceCalculatorFactory.getCalculator(DistanceMetric.MANHATTAN);
        assertEquals(2.0, manhattan.calculate(v1, v2), 1e-9);
    }

    @Test
    void testCosineDistanceCalculatesCorrectlyForParallelVectors() {
        Vector v1 = new Vector(1L, new float[]{3.0f, 4.0f}, 100L);
        Vector v2 = new Vector(2L, new float[]{6.0f, 8.0f}, 101L); // Parallel

        DistanceCalculator cosine = DistanceCalculatorFactory.getCalculator(DistanceMetric.COSINE);
        // Parallel vectors have cosine similarity of 1.0, so cosine distance should be 0.0
        assertEquals(0.0, cosine.calculate(v1, v2), 1e-6);
    }

    @Test
    void testDistanceCalculatorFactoryReturnsCorrectClasses() {
        assertTrue(DistanceCalculatorFactory.getCalculator(DistanceMetric.COSINE) instanceof CosineDistanceCalculator);
        assertTrue(DistanceCalculatorFactory.getCalculator(DistanceMetric.EUCLIDEAN) instanceof EuclideanDistanceCalculator);
        assertTrue(DistanceCalculatorFactory.getCalculator(DistanceMetric.MANHATTAN) instanceof ManhattanDistanceCalculator);
        assertThrows(SearchException.class, () -> DistanceCalculatorFactory.getCalculator(null));
    }
}
