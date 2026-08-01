package com.djimbinov.platform.organization.controller;

import com.djimbinov.platform.common.ApiResponse;
import com.djimbinov.platform.organization.dto.OrganizationRequest;
import com.djimbinov.platform.organization.dto.OrganizationResponse;
import com.djimbinov.platform.organization.service.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

  private final OrganizationService organizationService;

  public OrganizationController(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<OrganizationResponse>> create(
        @RequestBody OrganizationRequest request
  ) {
    OrganizationResponse organization = organizationService.create(request);

    return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(ApiResponse.success(
                "Organization created successfully",
                organization
          ));
  }

  @GetMapping
  public ApiResponse<List<OrganizationResponse>> findAll() {
    return ApiResponse.success(
          "Organizations loaded successfully",
          organizationService.findAll()
    );
  }
}