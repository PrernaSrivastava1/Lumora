package com.lumora.service;

import com.lumora.dto.WorkspaceDto;
import com.lumora.dto.WorkspaceResponseDto;
import com.lumora.model.Workspace;
import com.lumora.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private WorkspaceService workspaceService;

    private Workspace sampleWorkspace;

    @BeforeEach
    void setUp() {
        sampleWorkspace = Workspace.builder()
                .id(1L)
                .name("Test Workspace")
                .description("Test Description")
                .totalDocuments(5)
                .totalVectors(50)
                .build();
        sampleWorkspace.setCreatedAt(LocalDateTime.now());
        sampleWorkspace.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getAllWorkspaces_ShouldReturnDtoList() {
        when(workspaceRepository.findAll()).thenReturn(Arrays.asList(sampleWorkspace));

        List<WorkspaceResponseDto> result = workspaceService.getAllWorkspaces();

        assertEquals(1, result.size());
        assertEquals("Test Workspace", result.get(0).getName());
        assertEquals(5, result.get(0).getTotalDocuments());
        verify(workspaceRepository, times(1)).findAll();
    }

    @Test
    void getWorkspaceById_ShouldReturnDto() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(sampleWorkspace));

        WorkspaceResponseDto result = workspaceService.getWorkspaceById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Workspace", result.getName());
    }

    @Test
    void getWorkspaceById_NotFound_ShouldThrow() {
        when(workspaceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> workspaceService.getWorkspaceById(999L));
    }

    @Test
    void createWorkspace_ShouldReturnDto() {
        WorkspaceDto input = WorkspaceDto.builder()
                .name("New Workspace")
                .description("New Description")
                .build();

        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(inv -> {
            Workspace ws = inv.getArgument(0);
            ws.setId(2L);
            ws.setCreatedAt(LocalDateTime.now());
            ws.setUpdatedAt(LocalDateTime.now());
            return ws;
        });

        WorkspaceResponseDto result = workspaceService.createWorkspace(input);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("New Workspace", result.getName());
        verify(workspaceRepository, times(1)).save(any(Workspace.class));
    }

    @Test
    void updateWorkspace_ShouldReturnUpdatedDto() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(sampleWorkspace));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkspaceDto update = WorkspaceDto.builder()
                .name("Updated Name")
                .description("Updated Desc")
                .build();

        WorkspaceResponseDto result = workspaceService.updateWorkspace(1L, update);

        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Desc", result.getDescription());
    }

    @Test
    void deleteWorkspace_NotFound_ShouldThrow() {
        when(workspaceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> workspaceService.deleteWorkspace(999L));
    }

    @Test
    void deleteWorkspace_Existing_ShouldCallDelete() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(sampleWorkspace));

        workspaceService.deleteWorkspace(1L);

        verify(workspaceRepository, times(1)).delete(sampleWorkspace);
    }
}
