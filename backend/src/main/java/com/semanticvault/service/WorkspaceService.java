package com.semanticvault.service;

import com.semanticvault.dto.EntityMapper;
import com.semanticvault.dto.WorkspaceDto;
import com.semanticvault.dto.WorkspaceResponseDto;
import com.semanticvault.model.Workspace;
import com.semanticvault.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponseDto> getAllWorkspaces() {
        return workspaceRepository.findAll().stream()
                .map(EntityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkspaceResponseDto getWorkspaceById(Long id) {
        Workspace workspace = findEntityById(id);
        return EntityMapper.toDto(workspace);
    }

    public WorkspaceResponseDto createWorkspace(WorkspaceDto dto) {
        Workspace workspace = Workspace.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .totalDocuments(0)
                .totalVectors(0)
                .build();
        Workspace saved = workspaceRepository.save(workspace);
        return EntityMapper.toDto(saved);
    }

    public WorkspaceResponseDto updateWorkspace(Long id, WorkspaceDto dto) {
        Workspace existing = findEntityById(id);
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        Workspace saved = workspaceRepository.save(existing);
        return EntityMapper.toDto(saved);
    }

    public void deleteWorkspace(Long id) {
        Workspace workspace = findEntityById(id);
        workspaceRepository.delete(workspace); // triggers soft-delete via @SQLDelete
    }

    /**
     * Internal helper: returns the raw entity for service-layer use only.
     */
    public Workspace findEntityById(Long id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found with ID: " + id));
    }
}
