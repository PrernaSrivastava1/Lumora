package com.semanticvault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.semanticvault.dto.WorkspaceDto;
import com.semanticvault.repository.WorkspaceRepository;
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
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        workspaceRepository.deleteAll();
    }

    @Test
    void testCreateWorkspaceAndRetrieveIt() throws Exception {
        WorkspaceDto dto = WorkspaceDto.builder()
                .name("New Workspace")
                .description("Unique workspace details description")
                .build();

        mockMvc.perform(post("/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.name", is("New Workspace")));

        mockMvc.perform(get("/workspaces"))
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

        mockMvc.perform(post("/workspaces")
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
        mockMvc.perform(post("/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creation)));

        WorkspaceDto update = WorkspaceDto.builder().name("Updated Name").description("Updated description").build();
        mockMvc.perform(put("/workspaces/1")
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
        mockMvc.perform(post("/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creation)));

        mockMvc.perform(delete("/workspaces/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        mockMvc.perform(get("/workspaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void testGetWorkspaceNotFoundReturns400() throws Exception {
        mockMvc.perform(get("/workspaces/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Workspace not found with ID: 99")));
    }
}
