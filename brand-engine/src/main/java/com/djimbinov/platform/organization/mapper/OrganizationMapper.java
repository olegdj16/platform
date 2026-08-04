package com.djimbinov.platform.organization.mapper;

import com.djimbinov.platform.organization.dto.OrganizationRequest;
import com.djimbinov.platform.organization.dto.OrganizationResponse;
import com.djimbinov.platform.organization.model.Organization;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class OrganizationMapper {

  public Organization toModel(OrganizationRequest request) {
    Instant now = Instant.now();

    return new Organization(
          UUID.randomUUID(),
          request.getName(),
          now,
          now
    );
  }

  public OrganizationResponse toResponse(Organization organization) {
    return new OrganizationResponse(
          organization.getId(),
          organization.getName()
    );
  }
}