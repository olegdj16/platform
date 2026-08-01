package com.djimbinov.platform.organization.service;

import com.djimbinov.platform.organization.dto.OrganizationRequest;
import com.djimbinov.platform.organization.dto.OrganizationResponse;
import com.djimbinov.platform.organization.mapper.OrganizationMapper;
import com.djimbinov.platform.organization.model.Organization;
import com.djimbinov.platform.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final OrganizationMapper organizationMapper;

  public OrganizationService(
        OrganizationRepository organizationRepository,
        OrganizationMapper organizationMapper
  ) {
    this.organizationRepository = organizationRepository;
    this.organizationMapper = organizationMapper;
  }

  public OrganizationResponse create(OrganizationRequest request) {
    Organization organization = organizationMapper.toModel(request);
    Organization savedOrganization = organizationRepository.save(organization);

    return organizationMapper.toResponse(savedOrganization);
  }

  public List<OrganizationResponse> findAll() {
    return organizationRepository.findAll()
          .stream()
          .map(organizationMapper::toResponse)
          .toList();
  }
}