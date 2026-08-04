package com.djimbinov.platform.organization.exception;

import java.util.UUID;

public class OrganizationNotFoundException extends RuntimeException {

  public OrganizationNotFoundException(UUID organizationId) {
    super("Organization not found: " + organizationId);
  }
}