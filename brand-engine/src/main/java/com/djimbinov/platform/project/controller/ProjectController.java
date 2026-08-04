package com.djimbinov.platform.project.controller;

import com.djimbinov.platform.common.ApiResponse;
import com.djimbinov.platform.project.dto.ProjectRequest;
import com.djimbinov.platform.project.dto.ProjectResponse;
import com.djimbinov.platform.project.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
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
}