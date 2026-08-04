package com.djimbinov.platform.project.mapper;

import com.djimbinov.platform.organization.model.Organization;
import com.djimbinov.platform.project.dto.ProjectRequest;
import com.djimbinov.platform.project.dto.ProjectResponse;
import com.djimbinov.platform.project.model.Project;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ProjectMapper {

  public Project toModel(
        ProjectRequest request,
        Organization organization
  ) {

    return new Project(
          UUID.randomUUID(),
          request.getName(),
          request.getDescription(),
          Instant.now(),
          organization
    );
  }

  public ProjectResponse toResponse(Project project) {

    return new ProjectResponse(
          project.getId(),
          project.getOrganization().getId(),
          project.getName(),
          project.getDescription(),
          project.getCreatedAt()
    );
  }
}