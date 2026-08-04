package com.djimbinov.platform.project.service;

import com.djimbinov.platform.organization.repository.OrganizationRepository;
import com.djimbinov.platform.project.dto.ProjectResponse;
import com.djimbinov.platform.project.exception.ProjectNotFoundException;
import com.djimbinov.platform.project.mapper.ProjectMapper;
import com.djimbinov.platform.project.model.Project;
import com.djimbinov.platform.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private OrganizationRepository organizationRepository;

  @Mock
  private ProjectMapper projectMapper;

  private ProjectService projectService;

  @BeforeEach
  void setUp() {
    projectService = new ProjectService(
          projectRepository,
          organizationRepository,
          projectMapper
    );
  }

  @Test
  void shouldReturnProjectWhenProjectExists() {

    Project project = new Project(
          UUID.randomUUID(),
          "Brand Engine",
          "Testing",
          Instant.now(),
          null
    );

    ProjectResponse response = new ProjectResponse(
          project.getId(),
          null,
          "Brand Engine",
          "Testing",
          project.getCreatedAt()
    );

    when(projectRepository.findById(project.getId()))
          .thenReturn(Optional.of(project));

    when(projectMapper.toResponse(project))
          .thenReturn(response);

    ProjectResponse result =
          projectService.findById(project.getId());

    assertEquals(project.getName(), result.getName());

    verify(projectRepository).findById(project.getId());
    verify(projectMapper).toResponse(project);
  }

  @Test
  void shouldThrowProjectNotFoundExceptionWhenProjectDoesNotExist() {
    UUID projectId = UUID.randomUUID();

    when(projectRepository.findById(projectId))
          .thenReturn(Optional.empty());

    ProjectNotFoundException exception = assertThrows(
          ProjectNotFoundException.class,
          () -> projectService.findById(projectId)
    );

    assertEquals(
          "Project not found: " + projectId,
          exception.getMessage()
    );

    verify(projectRepository).findById(projectId);

    verify(projectMapper, never()).toResponse(any());
  }
}