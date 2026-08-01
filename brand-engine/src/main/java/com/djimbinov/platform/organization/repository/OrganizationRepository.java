package com.djimbinov.platform.organization.repository;

import com.djimbinov.platform.organization.model.Organization;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class OrganizationRepository {

  private final List<Organization> organizations = new ArrayList<>();

  public Organization save(Organization organization) {
    organizations.add(organization);
    return organization;
  }

  public List<Organization> findAll() {
    return organizations;
  }
}