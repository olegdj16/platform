package com.djimbinov.platform.project.service;

import com.djimbinov.platform.organization.model.Organization;
import com.djimbinov.platform.organization.repository.OrganizationRepository;
import com.djimbinov.platform.project.dto.ProjectRequest;
import com.djimbinov.platform.project.dto.ProjectResponse;
import com.djimbinov.platform.project.mapper.ProjectMapper;
import com.djimbinov.platform.project.model.Project;
import com.djimbinov.platform.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.djimbinov.platform.organization.exception.OrganizationNotFoundException;
import com.djimbinov.platform.project.exception.ProjectAlreadyExistsException;
import com.djimbinov.platform.project.exception.ProjectNotFoundException;
import java.util.UUID;

@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final OrganizationRepository organizationRepository;
  private final ProjectMapper projectMapper;

  public ProjectService(
        ProjectRepository projectRepository,
        OrganizationRepository organizationRepository,
        ProjectMapper projectMapper
  ) {
    this.projectRepository = projectRepository;
    this.organizationRepository = organizationRepository;
    this.projectMapper = projectMapper;
  }

  @Transactional
  public ProjectResponse create(ProjectRequest request) {
    Organization organization = organizationRepository
          .findById(request.getOrganizationId())
          .orElseThrow(() -> new OrganizationNotFoundException(
                request.getOrganizationId()
          ));

    boolean projectExists =
          projectRepository.existsByOrganizationIdAndNameIgnoreCase(
                request.getOrganizationId(),
                request.getName()
          );

    if (projectExists) {
      throw new ProjectAlreadyExistsException(request.getName());
    }

    Project project = projectMapper.toModel(request, organization);
    Project savedProject = projectRepository.save(project);

    return projectMapper.toResponse(savedProject);
  }

  @Transactional(readOnly = true)
  public List<ProjectResponse> findAll() {
    return projectRepository.findAll()
          .stream()
          .map(projectMapper::toResponse)
          .toList();
  }

  @Transactional(readOnly = true)
  public ProjectResponse findById(UUID id) {
    Project project = projectRepository.findById(id)
          .orElseThrow(() -> new ProjectNotFoundException(id));

    return projectMapper.toResponse(project);
  }

  @Transactional
  public ProjectResponse update(UUID id, ProjectRequest request) {

    Project project = projectRepository.findById(id)
          .orElseThrow(() -> new ProjectNotFoundException(id));

    Organization organization = organizationRepository
          .findById(request.getOrganizationId())
          .orElseThrow(() -> new OrganizationNotFoundException(
                request.getOrganizationId()
          ));

    projectMapper.updateModel(project, request, organization);

    Project updatedProject = projectRepository.save(project);

    return projectMapper.toResponse(updatedProject);
  }

  @Transactional
  public void delete(UUID id) {
    Project project = projectRepository.findById(id)
          .orElseThrow(() -> new ProjectNotFoundException(id));

    projectRepository.delete(project);
  }
}

