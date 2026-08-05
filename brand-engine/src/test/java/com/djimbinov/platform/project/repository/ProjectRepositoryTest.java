package com.djimbinov.platform.project.repository;

import com.djimbinov.platform.organization.model.Organization;
import com.djimbinov.platform.project.model.Project;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import com.djimbinov.platform.organization.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(
      replace = AutoConfigureTestDatabase.Replace.NONE
)
class ProjectRepositoryTest {

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Test
  void shouldReturnTrueWhenProjectNameAlreadyExists() {
    Instant now = Instant.now();

    Organization organization = organizationRepository.save(
          new Organization(
                UUID.randomUUID(),
                "Acme",
                now,
                now
          )
    );

    Project project = new Project(
          UUID.randomUUID(),
          "Brand Engine",
          "description",
          now,
          organization
    );

    projectRepository.save(project);

    boolean exists = projectRepository
          .existsByOrganizationIdAndNameIgnoreCase(
                organization.getId(),
                "brand engine"
          );

    assertTrue(exists);
  }

  @Test
  void shouldReturnFalseWhenProjectNameDoesNotExist() {
    Instant now = Instant.now();

    Organization organization = organizationRepository.save(
          new Organization(
                UUID.randomUUID(),
                "Acme",
                now,
                now
          )
    );

    boolean exists = projectRepository
          .existsByOrganizationIdAndNameIgnoreCase(
                organization.getId(),
                "Missing Project"
          );

    assertFalse(exists);
  }

  @Test
  void shouldSaveProject() {
    Instant now = Instant.now();

    Organization organization = organizationRepository.save(
          new Organization(
                UUID.randomUUID(),
                "Acme",
                now,
                now
          )
    );

    Project project = new Project(
          UUID.randomUUID(),
          "Brand Engine",
          "description",
          now,
          organization
    );

    Project saved = projectRepository.save(project);

    assertNotNull(saved.getId());
    assertEquals("Brand Engine", saved.getName());
    assertEquals(organization.getId(),
          saved.getOrganization().getId());
  }
}
