package com.lumora.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumora.dto.WorkspaceDto;
import com.lumora.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.security.test.context.support.WithMockUser(username = "testuser")
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private com.lumora.repository.UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM refresh_tokens");
        jdbcTemplate.execute("DELETE FROM user_profiles");
        jdbcTemplate.execute("DELETE FROM user_roles");
        jdbcTemplate.execute("DELETE FROM vector_embeddings");
        jdbcTemplate.execute("DELETE FROM document_chunks");
        jdbcTemplate.execute("DELETE FROM documents");
        jdbcTemplate.execute("DELETE FROM workspaces");
        jdbcTemplate.execute("DELETE FROM users");
        
        com.lumora.model.User testUser = com.lumora.model.User.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password")
                .roles(java.util.Set.of(com.lumora.model.Role.ROLE_USER))
                .build();
        userRepository.saveAndFlush(testUser);
    }

    @Test
    void testCreateWorkspaceAndRetrieveIt() throws Exception {
        WorkspaceDto dto = WorkspaceDto.builder()
                .name("New Workspace")
                .description("Unique workspace details description")
                .build();

        mockMvc.perform(post("/api/v1/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name", is("New Workspace")));

        mockMvc.perform(get("/api/v1/workspaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("New Workspace")));
    }

    @Test
    void testCreateWorkspaceValidationError() throws Exception {
        WorkspaceDto invalidDto = WorkspaceDto.builder()
                .name("") // Empty name violates @NotBlank
                .description("Desc")
                .build();

        mockMvc.perform(post("/api/v1/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Validation error occurred")));
    }

    @Test
    void testUpdateWorkspaceDetails() throws Exception {
        // Pre-create a workspace first
        WorkspaceDto creation = WorkspaceDto.builder().name("Original").description("Original desc").build();
        String result = mockMvc.perform(post("/api/v1/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creation)))
                .andReturn().getResponse().getContentAsString();
        Long workspaceId = ((Number) com.jayway.jsonpath.JsonPath.read(result, "$.data.id")).longValue();

        WorkspaceDto update = WorkspaceDto.builder().name("Updated Name").description("Updated description").build();
        mockMvc.perform(put("/api/v1/workspaces/" + workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Updated Name")))
                .andExpect(jsonPath("$.data.description", is("Updated description")));
    }

    @Test
    void testDeleteWorkspaceEndpoint() throws Exception {
        WorkspaceDto creation = WorkspaceDto.builder().name("To Delete").build();
        String result = mockMvc.perform(post("/api/v1/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creation)))
                .andReturn().getResponse().getContentAsString();
        Long workspaceId = ((Number) com.jayway.jsonpath.JsonPath.read(result, "$.data.id")).longValue();

        mockMvc.perform(delete("/api/v1/workspaces/" + workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        mockMvc.perform(get("/api/v1/workspaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void testGetWorkspaceNotFoundReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Workspace not found with ID: 99")));
    }
}
