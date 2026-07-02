package com.semanticvault.controller;

import com.semanticvault.dto.ApiResponse;
import com.semanticvault.dto.WorkspaceDto;
import com.semanticvault.model.Workspace;
import com.semanticvault.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    public ApiResponse<List<Workspace>> getAll() {
        List<Workspace> list = workspaceService.getAllWorkspaces();
        return ApiResponse.success("Retrieved all workspaces", list);
    }

    @GetMapping("/{id}")
    public ApiResponse<Workspace> getById(@PathVariable Long id) {
        Workspace ws = workspaceService.getWorkspaceById(id);
        return ApiResponse.success("Retrieved workspace details", ws);
    }

    @PostMapping
    public ApiResponse<Workspace> create(@Valid @RequestBody WorkspaceDto dto) {
        Workspace ws = workspaceService.createWorkspace(dto);
        return ApiResponse.success("Workspace created successfully", ws);
    }

    @PutMapping("/{id}")
    public ApiResponse<Workspace> update(@PathVariable Long id, @Valid @RequestBody WorkspaceDto dto) {
        Workspace ws = workspaceService.updateWorkspace(id, dto);
        return ApiResponse.success("Workspace updated successfully", ws);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        workspaceService.deleteWorkspace(id);
        return ApiResponse.success("Workspace deleted successfully", null);
    }
}
