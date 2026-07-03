package com.semanticvault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.semanticvault.algorithms.common.vector.Vector;
import com.semanticvault.model.AlgorithmType;
import com.semanticvault.model.DistanceMetric;
import com.semanticvault.model.Document;
import com.semanticvault.model.DocumentChunk;
import com.semanticvault.model.SearchRequest;
import com.semanticvault.model.Workspace;
import com.semanticvault.repository.ChunkRepository;
import com.semanticvault.repository.DocumentRepository;
import com.semanticvault.repository.VectorStore;
import com.semanticvault.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private VectorStore vectorStore;

    private Workspace workspace;
    private DocumentChunk chunk;

    @BeforeEach
    void setUp() {
        vectorStore.getIndexManager().clearAll();

        workspace = Workspace.builder().name("Search Test Workspace").build();
        workspace = workspaceRepository.save(workspace);

        Document doc = Document.builder()
                .workspace(workspace)
                .title("FAQ Document")
                .originalFileName("faq.txt")
                .processingStatus(com.semanticvault.model.ProcessingStatus.READY)
                .build();
        doc = documentRepository.save(doc);

        chunk = DocumentChunk.builder()
                .document(doc)
                .chunkIndex(0)
                .content("Vite is a next-generation frontend tool that is extremely fast.")
                .build();
        chunk = chunkRepository.save(chunk);

        // Add corresponding vector to the workspace index
        Vector v = new Vector(chunk.getId(), new float[]{0.1f, 0.2f, 0.3f}, 999L);
        vectorStore.add(workspace.getId(), v);
    }

    @Test
    void testSearchEndpointWithRawDimensionsSuccess() throws Exception {
        SearchRequest request = SearchRequest.builder()
                .workspaceId(workspace.getId())
                .algorithm(AlgorithmType.BRUTE_FORCE)
                .metric(DistanceMetric.EUCLIDEAN)
                .query("0.1, 0.2, 0.3")
                .topK(5)
                .build();

        mockMvc.perform(post("/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.query", is("0.1, 0.2, 0.3")))
                .andExpect(jsonPath("$.data.algorithm", is("BRUTE_FORCE")))
                .andExpect(jsonPath("$.data.metric", is("EUCLIDEAN")))
                .andExpect(jsonPath("$.data.results", hasSize(1)))
                .andExpect(jsonPath("$.data.results[0].chunkId", is(chunk.getId().intValue())))
                .andExpect(jsonPath("$.data.results[0].matchedText", containsString("Vite is a next-generation")))
                .andExpect(jsonPath("$.data.results[0].score", closeTo(0.0, 1e-6)));
    }

    @Test
    void testSearchEndpointWithNaturalTextQuerySuccess() throws Exception {
        // Will trigger MockEmbeddingProvider since active provider is MOCK in application.yml
        SearchRequest request = SearchRequest.builder()
                .workspaceId(workspace.getId())
                .algorithm(AlgorithmType.BRUTE_FORCE)
                .metric(DistanceMetric.COSINE)
                .query("Vite frontend speed")
                .topK(1)
                .build();

        mockMvc.perform(post("/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.results", hasSize(1)))
                .andExpect(jsonPath("$.data.results[0].matchedText", notNullValue()));
    }

    @Test
    void testSearchEndpointWithInvalidParamsReturnsError() throws Exception {
        SearchRequest request = SearchRequest.builder()
                .workspaceId(null) // Invalid: required
                .algorithm(AlgorithmType.BRUTE_FORCE)
                .metric(DistanceMetric.COSINE)
                .query("") // Invalid: blank
                .topK(0) // Invalid: min 1
                .build();

        mockMvc.perform(post("/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
