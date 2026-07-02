package com.semanticvault.algorithms.common.vector;

/**
 * Calculates Cosine Distance (1.0 - Cosine Similarity) between two vectors.
 */
public class CosineDistanceCalculator implements DistanceCalculator {

    @Override
    public double calculate(Vector a, Vector b) {
        VectorValidator.validateMatchingDimensions(a, b);

        double dot = VectorUtils.dotProduct(a.getValues(), b.getValues());
        double magA = a.magnitude();
        double magB = b.magnitude();

        if (magA < 1e-9 || magB < 1e-9) {
            return 1.0; // Max distance if vector is near-zero
        }

        double similarity = dot / (magA * magB);
        // Clamp to avoid float precision issues beyond [-1.0, 1.0] range
        similarity = Math.max(-1.0, Math.min(1.0, similarity));

        return 1.0 - similarity;
    }
}
