package com.semanticvault.service;

import com.semanticvault.dto.WorkspaceDto;
import com.semanticvault.model.Workspace;
import com.semanticvault.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    public List<Workspace> getAllWorkspaces() {
        return workspaceRepository.findAll();
    }

    public Workspace getWorkspaceById(Long id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found with ID: " + id));
    }

    public Workspace createWorkspace(WorkspaceDto dto) {
        Workspace workspace = Workspace.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .totalDocuments(0)
                .totalVectors(0)
                .build();
        return workspaceRepository.save(workspace);
    }

    public Workspace updateWorkspace(Long id, WorkspaceDto dto) {
        Workspace existing = getWorkspaceById(id);
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setUpdatedAt(LocalDateTime.now());
        return workspaceRepository.save(existing);
    }

    public void deleteWorkspace(Long id) {
        if (!workspaceRepository.existsById(id)) {
            throw new IllegalArgumentException("Workspace not found with ID: " + id);
        }
        workspaceRepository.deleteById(id);
    }
}
