package com.djimbinov.platform.organization.dto;

public class OrganizationRequest {

  private String name;

  public OrganizationRequest() {
  }

  public OrganizationRequest(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}