package com.lumora.algorithms.common.vector;

/**
 * Calculates Euclidean (L2) Distance between two vectors.
 */
public class EuclideanDistanceCalculator implements DistanceCalculator {

    @Override
    public double calculate(Vector a, Vector b) {
        VectorValidator.validateMatchingDimensions(a, b);

        float[] aVals = a.getValues();
        float[] bVals = b.getValues();
        double sum = 0.0;

        for (int i = 0; i < aVals.length; i++) {
            double diff = aVals[i] - bVals[i];
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }
}
