package com.lumora.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumora.dto.RagRequest;
import com.lumora.model.*;
import com.lumora.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.security.test.context.support.WithMockUser(username = "testuser")
class RagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private Workspace workspace;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("DELETE FROM refresh_tokens");
        jdbcTemplate.execute("DELETE FROM user_profiles");
        jdbcTemplate.execute("DELETE FROM user_roles");
        jdbcTemplate.execute("DELETE FROM chat_messages");
        jdbcTemplate.execute("DELETE FROM chat_sessions");
        jdbcTemplate.execute("DELETE FROM search_histories");
        jdbcTemplate.execute("DELETE FROM vector_embeddings");
        jdbcTemplate.execute("DELETE FROM document_chunks");
        jdbcTemplate.execute("DELETE FROM documents");
        jdbcTemplate.execute("DELETE FROM workspaces");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        User testUser = User.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password")
                .roles(java.util.Set.of(Role.ROLE_USER))
                .build();
        testUser = userRepository.saveAndFlush(testUser);

        workspace = Workspace.builder()
                .name("RAG Workspace")
                .owner(testUser)
                .build();
        workspace = workspaceRepository.saveAndFlush(workspace);

        Document doc = Document.builder()
                .workspace(workspace)
                .title("Company Guidelines")
                .originalFileName("guidelines.txt")
                .processingStatus(ProcessingStatus.READY)
                .build();
        doc = documentRepository.saveAndFlush(doc);

        DocumentChunk chunk = DocumentChunk.builder()
                .document(doc)
                .content("Our office hours are from 9 AM to 5 PM, Monday through Friday.")
                .chunkIndex(0)
                .build();
        chunkRepository.saveAndFlush(chunk);
    }

    @Test
    void testExecuteRagChatFlow() throws Exception {
        RagRequest request = RagRequest.builder()
                .query("What are the office hours?")
                .workspaceId(workspace.getId())
                .algorithm("BRUTE_FORCE")
                .topK(2)
                .build();

        mockMvc.perform(post("/api/v1/rag/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.answer", notNullValue()))
                .andExpect(jsonPath("$.data.sources", hasSize(greaterThanOrEqualTo(0))));
    }
}
