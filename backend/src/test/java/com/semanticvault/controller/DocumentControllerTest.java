package com.semanticvault.controller;

import com.semanticvault.dto.WorkspaceDto;
import com.semanticvault.model.Workspace;
import com.semanticvault.repository.DocumentRepository;
import com.semanticvault.repository.WorkspaceRepository;
import com.semanticvault.service.WorkspaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private DocumentRepository documentRepository;

    private Workspace testWorkspace;

    @BeforeEach
    void setUp() {
        documentRepository.clear();
        workspaceRepository.clear();
        testWorkspace = workspaceService.createWorkspace(WorkspaceDto.builder()
                .name("Demo Space")
                .description("Desc")
                .build());
    }

    @Test
    void testUploadAndRetrieveDocument() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "sample.txt",
                "text/plain",
                "Hello, semantic vector world!".getBytes()
        );

        mockMvc.perform(multipart("/documents")
                        .file(mockFile)
                        .param("workspaceId", String.valueOf(testWorkspace.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.title", is("sample.txt")))
                .andExpect(jsonPath("$.data.fileType", is("TXT")));

        mockMvc.perform(get("/documents")
                        .param("workspaceId", String.valueOf(testWorkspace.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title", is("sample.txt")));
    }

    @Test
    void testUploadUnsupportedFileTypeReturnsBadRequest() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/octet-stream",
                "binary".getBytes()
        );

        mockMvc.perform(multipart("/documents")
                        .file(mockFile)
                        .param("workspaceId", String.valueOf(testWorkspace.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Unsupported file type. Supported types: PDF, DOCX, TXT, MD")));
    }

    @Test
    void testDeleteDocumentRemovesMetadata() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "notes.md",
                "text/markdown",
                "## Notes".getBytes()
        );

        mockMvc.perform(multipart("/documents")
                .file(mockFile)
                .param("workspaceId", String.valueOf(testWorkspace.getId())));

        // Verify loaded doc has ID 1L
        mockMvc.perform(delete("/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        mockMvc.perform(get("/documents")
                        .param("workspaceId", String.valueOf(testWorkspace.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }
}
