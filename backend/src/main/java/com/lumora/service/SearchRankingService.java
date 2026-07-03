package com.lumora.service;

import com.lumora.model.SearchResult;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for ranking, sorting, and slicing similarity results.
 */
@Service
public class SearchRankingService {

    /**
     * Ranks and sorts the raw results by similarity distance (ascending order).
     * Returns the Top-K closest matches.
     *
     * @param rawResults a list of all calculated search results
     * @param topK the number of items to return
     * @return the Top-K sorted search results
     */
    public List<SearchResult> rank(List<SearchResult> rawResults, int topK) {
        if (rawResults == null || topK <= 0) {
            return List.of();
        }

        return rawResults.stream()
                .sorted(Comparator.comparingDouble(SearchResult::getScore))
                .limit(topK)
                .collect(Collectors.toList());
    }
}
