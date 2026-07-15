package com.lumora.controller;

import com.lumora.dto.ApiResponse;
import com.lumora.dto.WorkspaceDto;
import com.lumora.dto.WorkspaceResponseDto;
import com.lumora.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    public ApiResponse<List<WorkspaceResponseDto>> getAll() {
        List<WorkspaceResponseDto> list = workspaceService.getAllWorkspaces();
        return ApiResponse.success("Retrieved all workspaces", list);
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkspaceResponseDto> getById(@PathVariable Long id) {
        WorkspaceResponseDto ws = workspaceService.getWorkspaceById(id);
        return ApiResponse.success("Retrieved workspace details", ws);
    }

    @PostMapping
    public ApiResponse<WorkspaceResponseDto> create(@Valid @RequestBody WorkspaceDto dto) {
        WorkspaceResponseDto ws = workspaceService.createWorkspace(dto);
        return ApiResponse.success("Workspace created successfully", ws);
    }

    @PutMapping("/{id}")
    public ApiResponse<WorkspaceResponseDto> update(@PathVariable Long id, @Valid @RequestBody WorkspaceDto dto) {
        WorkspaceResponseDto ws = workspaceService.updateWorkspace(id, dto);
        return ApiResponse.success("Workspace updated successfully", ws);
    }

    @GetMapping("/{id}/stats")
    public ApiResponse<java.util.Map<String, Object>> getStats(@PathVariable Long id) {
        java.util.Map<String, Object> stats = workspaceService.getWorkspaceStats(id);
        return ApiResponse.success("Retrieved workspace statistics", stats);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        workspaceService.deleteWorkspace(id);
        return ApiResponse.success("Workspace deleted successfully", null);
    }
}
