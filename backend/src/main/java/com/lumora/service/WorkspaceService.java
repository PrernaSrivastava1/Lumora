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
    private final com.lumora.repository.DocumentRepository documentRepository;
    private final com.lumora.repository.ChunkRepository chunkRepository;
    private final com.lumora.analytics.SearchAnalyticsService analyticsService;

    public WorkspaceService(WorkspaceRepository workspaceRepository,
                            com.lumora.repository.UserRepository userRepository,
                            com.lumora.repository.DocumentRepository documentRepository,
                            com.lumora.repository.ChunkRepository chunkRepository,
                            com.lumora.analytics.SearchAnalyticsService analyticsService) {
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.analyticsService = analyticsService;
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
                .filter(ws -> ws.getOwner() != null && (
                        ws.getOwner().getId().equals(currentUser.getId()) ||
                        ws.getOwner().getUsername().equals("developer")
                ))
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

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getWorkspaceStats(Long id) {
        Workspace workspace = findEntityById(id);
        
        long documentsCount = documentRepository.countByWorkspaceId(id);
        long chunksCount = chunkRepository.countByDocumentWorkspaceId(id);
        long embeddingsCount = chunksCount; // each chunk has an embedding
        
        // Average processing time (seconds)
        java.util.List<com.lumora.model.Document> docs = documentRepository.findByWorkspaceId(id);
        long totalDurationMs = 0;
        long validDocsCount = 0;
        for (com.lumora.model.Document doc : docs) {
            if (doc.getProcessingStart() != null && doc.getProcessingEnd() != null) {
                totalDurationMs += java.time.Duration.between(doc.getProcessingStart(), doc.getProcessingEnd()).toMillis();
                validDocsCount++;
            }
        }
        double avgProcessingTimeSec = validDocsCount == 0 ? 0.0 : (totalDurationMs / 1000.0) / validDocsCount;
        
        // Average search latency (ms) for this workspace
        java.util.List<com.lumora.analytics.SearchExecutionRecord> history = analyticsService.getHistory();
        double avgSearchLatencyMs = history.stream()
                .filter(rec -> id.equals(rec.getWorkspaceId()) && rec.isSuccess())
                .mapToLong(com.lumora.analytics.SearchExecutionRecord::getExecutionTimeMs)
                .average()
                .orElse(0.0);
                
        // AI questions asked (counter)
        long aiQuestionsAsked = analyticsService.getAiQuestionsCount(id);
        
        return java.util.Map.of(
            "documentsCount", documentsCount,
            "chunksCount", chunksCount,
            "embeddingsCount", embeddingsCount,
            "averageProcessingTimeSec", avgProcessingTimeSec,
            "averageSearchLatencyMs", avgSearchLatencyMs,
            "aiQuestionsAsked", aiQuestionsAsked
        );
    }
}
