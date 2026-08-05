package com.djimbinov.platform.organization.repository;

import com.djimbinov.platform.organization.model.Organization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(
      replace = AutoConfigureTestDatabase.Replace.NONE
)
class OrganizationRepositoryTest {

  @Autowired
  private OrganizationRepository organizationRepository;

  @Test
  void shouldSaveOrganization() {
    Instant now = Instant.now();

    Organization organization = new Organization(
          UUID.randomUUID(),
          "Acme",
          now,
          now
    );

    Organization saved =
          organizationRepository.saveAndFlush(organization);

    assertNotNull(saved.getId());
    assertEquals("Acme", saved.getName());
    assertEquals(now, saved.getCreatedAt());
    assertEquals(now, saved.getUpdatedAt());
  }

  @Test
  void shouldReturnOrganizationWhenOrganizationExists() {
    Instant now = Instant.now();

    Organization organization = organizationRepository.saveAndFlush(
          new Organization(
                UUID.randomUUID(),
                "Acme",
                now,
                now
          )
    );

    Optional<Organization> result =
          organizationRepository.findById(organization.getId());

    assertTrue(result.isPresent());
    assertEquals(organization.getId(), result.get().getId());
    assertEquals("Acme", result.get().getName());
  }

  @Test
  void shouldReturnEmptyWhenOrganizationDoesNotExist() {
    UUID organizationId = UUID.randomUUID();

    Optional<Organization> result =
          organizationRepository.findById(organizationId);

    assertFalse(result.isPresent());
  }

  @Test
  void shouldReturnAllOrganizations() {
    Instant now = Instant.now();

    organizationRepository.save(
          new Organization(
                UUID.randomUUID(),
                "Acme",
                now,
                now
          )
    );

    organizationRepository.save(
          new Organization(
                UUID.randomUUID(),
                "Globex",
                now,
                now
          )
    );

    organizationRepository.flush();

    List<Organization> results =
          organizationRepository.findAll();

    assertTrue(
          results.stream()
                .anyMatch(organization ->
                      "Acme".equals(organization.getName()))
    );

    assertTrue(
          results.stream()
                .anyMatch(organization ->
                      "Globex".equals(organization.getName()))
    );

    assertTrue(results.size() >= 2);

    assertTrue(
          results.stream()
                .anyMatch(organization ->
                      "Acme".equals(organization.getName()))
    );
    assertTrue(
          results.stream()
                .anyMatch(organization ->
                      "Globex".equals(organization.getName()))
    );
  }
}