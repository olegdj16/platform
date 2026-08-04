package com.djimbinov.platform.project.controller;

import com.djimbinov.platform.common.ApiResponse;
import com.djimbinov.platform.project.dto.ProjectRequest;
import com.djimbinov.platform.project.dto.ProjectResponse;
import com.djimbinov.platform.project.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ProjectResponse>> create(
        @Valid @RequestBody ProjectRequest request
  ) {
    ProjectResponse project = projectService.create(request);

    return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(ApiResponse.success(
                "Project created successfully",
                project
          ));
  }

  @GetMapping
  public ApiResponse<List<ProjectResponse>> findAll() {
    return ApiResponse.success(
          "Projects loaded successfully",
          projectService.findAll()
    );
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProjectResponse> findById(
        @PathVariable UUID id
  ) {
    return ResponseEntity.ok(projectService.findById(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProjectResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody ProjectRequest request
  ) {
    return ResponseEntity.ok(projectService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
        @PathVariable UUID id
  ) {
    projectService.delete(id);
    return ResponseEntity.noContent().build();
  }
}