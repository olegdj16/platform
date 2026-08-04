package com.djimbinov.platform.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class ProjectRequest {

  @NotNull(message = "Organization ID is required")
  private UUID organizationId;

  @NotBlank(message = "Project name is required")
  @Size(
        max = 150,
        message = "Project name must not exceed 150 characters"
  )
  private String name;

  @Size(
        max = 1000,
        message = "Project description must not exceed 1000 characters"
  )
  private String description;

  public ProjectRequest() {
  }

  public UUID getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(UUID organizationId) {
    this.organizationId = organizationId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}