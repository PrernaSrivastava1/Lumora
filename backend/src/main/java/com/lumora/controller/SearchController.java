package com.lumora.controller;

import com.lumora.algorithms.common.SearchEngine;
import com.lumora.dto.ApiResponse;
import com.lumora.model.SearchRequest;
import com.lumora.model.SearchResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing endpoints for executing similarity search queries.
 */
@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchEngine searchEngine;

    public SearchController(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    /**
     * Executes similarity searches across workspace vectors.
     *
     * @param request search parameters containing query text/dimensions
     * @return similarity results sorted by relevance
     */
    @PostMapping
    public ApiResponse<SearchResponse> executeSearch(@Valid @RequestBody SearchRequest request) {
        SearchResponse response = searchEngine.executeSearch(request);
        return ApiResponse.success("Search completed successfully", response);
    }
}
