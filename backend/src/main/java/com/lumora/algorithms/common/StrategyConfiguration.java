package com.lumora.algorithms.common;

import com.lumora.model.AlgorithmType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Spring configuration class that automatically resolves and registers
 * all available {@link SearchStrategy} implementations into the {@link SearchContext}.
 */
@Configuration
public class StrategyConfiguration {

    /**
     * Instantiates {@link SearchContext} using Spring-discovered search strategy list.
     *
     * @param strategyList list of all search strategy beans in the application context
     * @return registered SearchContext bean
     */
    @Bean
    public SearchContext searchContext(List<SearchStrategy> strategyList) {
        Map<AlgorithmType, SearchStrategy> strategiesMap = new EnumMap<>(AlgorithmType.class);
        for (SearchStrategy strategy : strategyList) {
            strategiesMap.put(strategy.getAlgorithmType(), strategy);
        }
        return new SearchContext(strategiesMap);
    }
}
