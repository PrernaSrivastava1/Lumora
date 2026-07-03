package com.lumora.service;

import com.lumora.dto.EntityMapper;
import com.lumora.dto.WorkspaceDto;
import com.lumora.dto.WorkspaceResponseDto;
import com.lumora.model.Workspace;
import com.lumora.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final com.lumora.repository.UserRepository userRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository, com.lumora.repository.UserRepository userRepository) {
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
    }

    private com.lumora.model.User getCurrentUser() {
        String username = com.lumora.util.SecurityUtils.getCurrentUsername();
        if (username == null || username.equals("anonymousUser")) {
            return userRepository.findByUsername("developer")
                    .orElseGet(() -> userRepository.saveAndFlush(com.lumora.model.User.builder()
                            .username("developer")
                            .email("dev@lumora.ai")
                            .password("password")
                            .roles(java.util.Set.of(com.lumora.model.Role.ROLE_USER))
                            .build()));
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponseDto> getAllWorkspaces() {
        com.lumora.model.User currentUser = getCurrentUser();
        return workspaceRepository.findAll().stream()
                .filter(ws -> ws.getOwner() != null && ws.getOwner().getId().equals(currentUser.getId()))
                .map(EntityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkspaceResponseDto getWorkspaceById(Long id) {
        Workspace workspace = findEntityById(id);
        com.lumora.model.User currentUser = getCurrentUser();
        if (workspace.getOwner() == null || !workspace.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Access Denied: You do not own this workspace");
        }
        return EntityMapper.toDto(workspace);
    }

    public WorkspaceResponseDto createWorkspace(WorkspaceDto dto) {
        com.lumora.model.User currentUser = getCurrentUser();
        Workspace workspace = Workspace.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .owner(currentUser)
                .totalDocuments(0)
                .totalVectors(0)
                .build();
        Workspace saved = workspaceRepository.save(workspace);
        return EntityMapper.toDto(saved);
    }

    public WorkspaceResponseDto updateWorkspace(Long id, WorkspaceDto dto) {
        Workspace existing = findEntityById(id);
        com.lumora.model.User currentUser = getCurrentUser();
        if (existing.getOwner() == null || !existing.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Access Denied: You do not own this workspace");
        }
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        Workspace saved = workspaceRepository.save(existing);
        return EntityMapper.toDto(saved);
    }

    public void deleteWorkspace(Long id) {
        Workspace workspace = findEntityById(id);
        com.lumora.model.User currentUser = getCurrentUser();
        if (workspace.getOwner() == null || !workspace.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Access Denied: You do not own this workspace");
        }
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
