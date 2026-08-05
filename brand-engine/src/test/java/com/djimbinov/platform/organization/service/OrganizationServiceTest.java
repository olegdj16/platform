package com.djimbinov.platform.organization.service;

import com.djimbinov.platform.organization.dto.OrganizationRequest;
import com.djimbinov.platform.organization.dto.OrganizationResponse;
import com.djimbinov.platform.organization.mapper.OrganizationMapper;
import com.djimbinov.platform.organization.model.Organization;
import com.djimbinov.platform.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

  @Mock
  private OrganizationRepository organizationRepository;

  @Mock
  private OrganizationMapper organizationMapper;

  private OrganizationService organizationService;

  @BeforeEach
  void setUp() {
    organizationService = new OrganizationService(
          organizationRepository,
          organizationMapper
    );
  }

  @Test
  void shouldCreateOrganizationWhenRequestIsValid() {
    UUID organizationId = UUID.randomUUID();

    OrganizationRequest request =
          new OrganizationRequest("Acme");

    Organization organization = mock(Organization.class);
    Organization savedOrganization = mock(Organization.class);

    OrganizationResponse expectedResponse =
          new OrganizationResponse(
                organizationId,
                "Acme"
          );

    when(organizationMapper.toModel(request))
          .thenReturn(organization);

    when(organizationRepository.save(organization))
          .thenReturn(savedOrganization);

    when(organizationMapper.toResponse(savedOrganization))
          .thenReturn(expectedResponse);

    OrganizationResponse result =
          organizationService.create(request);

    assertSame(expectedResponse, result);
    assertEquals(organizationId, result.getId());
    assertEquals("Acme", result.getName());

    verify(organizationMapper).toModel(request);
    verify(organizationRepository).save(organization);
    verify(organizationMapper).toResponse(savedOrganization);
  }

  @Test
  void shouldReturnAllOrganizations() {
    Organization firstOrganization = mock(Organization.class);
    Organization secondOrganization = mock(Organization.class);

    OrganizationResponse firstResponse =
          new OrganizationResponse(
                UUID.randomUUID(),
                "Acme"
          );

    OrganizationResponse secondResponse =
          new OrganizationResponse(
                UUID.randomUUID(),
                "Globex"
          );

    when(organizationRepository.findAll())
          .thenReturn(List.of(
                firstOrganization,
                secondOrganization
          ));

    when(organizationMapper.toResponse(firstOrganization))
          .thenReturn(firstResponse);

    when(organizationMapper.toResponse(secondOrganization))
          .thenReturn(secondResponse);

    List<OrganizationResponse> results =
          organizationService.findAll();

    assertEquals(2, results.size());
    assertEquals("Acme", results.get(0).getName());
    assertEquals("Globex", results.get(1).getName());

    verify(organizationRepository).findAll();
    verify(organizationMapper).toResponse(firstOrganization);
    verify(organizationMapper).toResponse(secondOrganization);
  }
}