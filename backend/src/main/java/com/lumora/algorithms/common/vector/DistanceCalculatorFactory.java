package com.lumora.algorithms.common.vector;

import com.lumora.algorithms.common.SearchException;
import com.lumora.model.DistanceMetric;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Factory class for retrieving distance calculators.
 * Implements a registry pattern using an EnumMap to avoid switch-case blocks.
 */
public final class DistanceCalculatorFactory {

    private static final Map<DistanceMetric, DistanceCalculator> CALCULATORS = new EnumMap<>(DistanceMetric.class);

    static {
        CALCULATORS.put(DistanceMetric.COSINE, new CosineDistanceCalculator());
        CALCULATORS.put(DistanceMetric.EUCLIDEAN, new EuclideanDistanceCalculator());
        CALCULATORS.put(DistanceMetric.MANHATTAN, new ManhattanDistanceCalculator());
    }

    private DistanceCalculatorFactory() {
        // Prevent instantiation of factory class
    }

    /**
     * Resolves the appropriate {@link DistanceCalculator} for a given {@link DistanceMetric}.
     *
     * @param metric distance metric classification
     * @return matching distance calculator
     * @throws SearchException if the metric is null or unsupported
     */
    public static DistanceCalculator getCalculator(DistanceMetric metric) {
        if (metric == null) {
            throw new SearchException("Distance metric cannot be null");
        }
        return Optional.ofNullable(CALCULATORS.get(metric))
                .orElseThrow(() -> new SearchException("Unsupported distance metric: " + metric));
    }
}
