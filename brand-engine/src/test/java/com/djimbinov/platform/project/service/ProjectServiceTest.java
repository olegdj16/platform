package com.djimbinov.platform.project.service;

import com.djimbinov.platform.organization.exception.OrganizationNotFoundException;
import com.djimbinov.platform.organization.model.Organization;
import com.djimbinov.platform.organization.repository.OrganizationRepository;
import com.djimbinov.platform.project.dto.ProjectRequest;
import com.djimbinov.platform.project.dto.ProjectResponse;
import com.djimbinov.platform.project.exception.ProjectAlreadyExistsException;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

  @Test
  void shouldCreateProjectWhenRequestIsValid() {
    UUID organizationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    Instant createdAt = Instant.now();

    ProjectRequest request = new ProjectRequest();
    request.setOrganizationId(organizationId);
    request.setName("Brand Engine");
    request.setDescription("AI-assisted brand research");

    Organization organization = mock(Organization.class);
    Project project = mock(Project.class);
    Project savedProject = mock(Project.class);

    ProjectResponse expectedResponse = new ProjectResponse(
          projectId,
          organizationId,
          request.getName(),
          request.getDescription(),
          createdAt
    );

    when(organizationRepository.findById(organizationId))
          .thenReturn(Optional.of(organization));

    when(projectRepository.existsByOrganizationIdAndNameIgnoreCase(
          organizationId,
          request.getName()
    )).thenReturn(false);

    when(projectMapper.toModel(request, organization))
          .thenReturn(project);

    when(projectRepository.save(project))
          .thenReturn(savedProject);

    when(projectMapper.toResponse(savedProject))
          .thenReturn(expectedResponse);

    ProjectResponse result = projectService.create(request);

    assertSame(expectedResponse, result);
    assertEquals(projectId, result.getId());
    assertEquals(organizationId, result.getOrganizationId());
    assertEquals("Brand Engine", result.getName());

    verify(organizationRepository).findById(organizationId);
    verify(projectRepository)
          .existsByOrganizationIdAndNameIgnoreCase(
                organizationId,
                request.getName()
          );
    verify(projectMapper).toModel(request, organization);
    verify(projectRepository).save(project);
    verify(projectMapper).toResponse(savedProject);
  }

  @Test
  void shouldThrowOrganizationNotFoundExceptionWhenCreatingProject() {

    UUID organizationId = UUID.randomUUID();

    ProjectRequest request = new ProjectRequest();
    request.setOrganizationId(organizationId);
    request.setName("Brand Engine");
    request.setDescription("Testing");

    when(organizationRepository.findById(organizationId))
          .thenReturn(Optional.empty());

    OrganizationNotFoundException exception = assertThrows(
          OrganizationNotFoundException.class,
          () -> projectService.create(request)
    );

    assertEquals(
          "Organization not found: " + organizationId,
          exception.getMessage()
    );

    verify(organizationRepository).findById(organizationId);

    verifyNoInteractions(projectRepository);
    verifyNoInteractions(projectMapper);
  }

  @Test
  void shouldThrowProjectAlreadyExistsExceptionWhenProjectNameAlreadyExists() {
    UUID organizationId = UUID.randomUUID();

    ProjectRequest request = new ProjectRequest();
    request.setOrganizationId(organizationId);
    request.setName("Brand Engine");
    request.setDescription("Duplicate project test");

    Organization organization = mock(Organization.class);

    when(organizationRepository.findById(organizationId))
          .thenReturn(Optional.of(organization));

    when(projectRepository.existsByOrganizationIdAndNameIgnoreCase(
          organizationId,
          request.getName()
    )).thenReturn(true);

    ProjectAlreadyExistsException exception = assertThrows(
          ProjectAlreadyExistsException.class,
          () -> projectService.create(request)
    );

    assertEquals(
          "A project named '" + request.getName()
                + "' already exists in this organization.",
          exception.getMessage()
    );

    verify(organizationRepository).findById(organizationId);

    verify(projectRepository)
          .existsByOrganizationIdAndNameIgnoreCase(
                organizationId,
                request.getName()
          );

    verify(projectRepository, never()).save(any());
    verifyNoInteractions(projectMapper);
  }

  @Test
  void shouldReturnAllProjects() {
    Project firstProject = mock(Project.class);
    Project secondProject = mock(Project.class);

    ProjectResponse firstResponse = new ProjectResponse(
          UUID.randomUUID(),
          UUID.randomUUID(),
          "Brand Engine",
          "First project",
          Instant.now()
    );

    ProjectResponse secondResponse = new ProjectResponse(
          UUID.randomUUID(),
          UUID.randomUUID(),
          "Second Project",
          "Second project",
          Instant.now()
    );

    when(projectRepository.findAll())
          .thenReturn(List.of(firstProject, secondProject));

    when(projectMapper.toResponse(firstProject))
          .thenReturn(firstResponse);

    when(projectMapper.toResponse(secondProject))
          .thenReturn(secondResponse);

    List<ProjectResponse> result = projectService.findAll();

    assertEquals(2, result.size());
    assertSame(firstResponse, result.get(0));
    assertSame(secondResponse, result.get(1));

    verify(projectRepository).findAll();
    verify(projectMapper).toResponse(firstProject);
    verify(projectMapper).toResponse(secondProject);
  }

  @Test
  void shouldUpdateProjectWhenRequestIsValid() {
    UUID projectId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();

    ProjectRequest request = new ProjectRequest();
    request.setOrganizationId(organizationId);
    request.setName("Updated Brand Engine");
    request.setDescription("Updated project description");

    Project project = mock(Project.class);
    Organization organization = mock(Organization.class);
    Project savedProject = mock(Project.class);

    ProjectResponse expectedResponse = new ProjectResponse(
          projectId,
          organizationId,
          request.getName(),
          request.getDescription(),
          Instant.now()
    );

    when(projectRepository.findById(projectId))
          .thenReturn(Optional.of(project));

    when(organizationRepository.findById(organizationId))
          .thenReturn(Optional.of(organization));

    when(projectRepository.save(project))
          .thenReturn(savedProject);

    when(projectMapper.toResponse(savedProject))
          .thenReturn(expectedResponse);

    ProjectResponse result =
          projectService.update(projectId, request);

    assertSame(expectedResponse, result);
    assertEquals("Updated Brand Engine", result.getName());

    verify(projectRepository).findById(projectId);
    verify(organizationRepository).findById(organizationId);
    verify(projectMapper).updateModel(
          project,
          request,
          organization
    );
    verify(projectRepository).save(project);
    verify(projectMapper).toResponse(savedProject);
  }

  @Test
  void shouldThrowProjectNotFoundExceptionWhenUpdatingMissingProject() {
    UUID projectId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();

    ProjectRequest request = new ProjectRequest();
    request.setOrganizationId(organizationId);
    request.setName("Updated Brand Engine");
    request.setDescription("Updated description");

    when(projectRepository.findById(projectId))
          .thenReturn(Optional.empty());

    ProjectNotFoundException exception = assertThrows(
          ProjectNotFoundException.class,
          () -> projectService.update(projectId, request)
    );

    assertEquals(
          "Project not found: " + projectId,
          exception.getMessage()
    );

    verify(projectRepository).findById(projectId);
    verifyNoInteractions(organizationRepository);
    verifyNoInteractions(projectMapper);
    verify(projectRepository, never()).save(any());
  }

  @Test
  void shouldThrowOrganizationNotFoundExceptionWhenUpdatingProject() {
    UUID projectId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();

    ProjectRequest request = new ProjectRequest();
    request.setOrganizationId(organizationId);
    request.setName("Updated Brand Engine");
    request.setDescription("Updated description");

    Project project = mock(Project.class);

    when(projectRepository.findById(projectId))
          .thenReturn(Optional.of(project));

    when(organizationRepository.findById(organizationId))
          .thenReturn(Optional.empty());

    OrganizationNotFoundException exception = assertThrows(
          OrganizationNotFoundException.class,
          () -> projectService.update(projectId, request)
    );

    assertEquals(
          "Organization not found: " + organizationId,
          exception.getMessage()
    );

    verify(projectRepository).findById(projectId);
    verify(organizationRepository).findById(organizationId);
    verify(projectMapper, never()).updateModel(any(), any(), any());
    verify(projectRepository, never()).save(any());
  }

  @Test
  void shouldDeleteProjectWhenProjectExists() {
    UUID projectId = UUID.randomUUID();
    Project project = mock(Project.class);

    when(projectRepository.findById(projectId))
          .thenReturn(Optional.of(project));

    projectService.delete(projectId);

    verify(projectRepository).findById(projectId);
    verify(projectRepository).delete(project);
    verifyNoInteractions(organizationRepository);
    verifyNoInteractions(projectMapper);
  }

  @Test
  void shouldThrowProjectNotFoundExceptionWhenDeletingMissingProject() {
    UUID projectId = UUID.randomUUID();

    when(projectRepository.findById(projectId))
          .thenReturn(Optional.empty());

    ProjectNotFoundException exception = assertThrows(
          ProjectNotFoundException.class,
          () -> projectService.delete(projectId)
    );

    assertEquals(
          "Project not found: " + projectId,
          exception.getMessage()
    );

    verify(projectRepository).findById(projectId);
    verify(projectRepository, never()).delete(any());
    verifyNoInteractions(organizationRepository);
    verifyNoInteractions(projectMapper);
  }
}