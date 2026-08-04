package com.djimbinov.platform.project.repository;

import com.djimbinov.platform.project.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

  boolean existsByOrganizationIdAndNameIgnoreCase(
        UUID organizationId,
        String name
  );
}