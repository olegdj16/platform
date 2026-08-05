package com.djimbinov.platform.document.repository;

import com.djimbinov.platform.document.model.Document;
import com.djimbinov.platform.organization.model.Organization;
import com.djimbinov.platform.organization.repository.OrganizationRepository;
import com.djimbinov.platform.project.model.Project;
import com.djimbinov.platform.project.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(
      replace = AutoConfigureTestDatabase.Replace.NONE
)
class DocumentRepositoryTest {

  @Autowired
  private DocumentRepository documentRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Test
  void shouldSaveDocument() {
    Instant now = Instant.now();

    Organization organization = saveOrganization(now);
    Project project = saveProject(organization, now);

    Document document = new Document(
          UUID.randomUUID(),
          "Architecture Diagram",
          "architecture.pdf",
          "application/pdf",
          "documents/architecture.pdf",
          245760L,
          now,
          project
    );

    Document saved = documentRepository.save(document);

    assertNotNull(saved.getId());
    assertEquals("Architecture Diagram", saved.getName());
    assertEquals("architecture.pdf", saved.getOriginalFilename());
    assertEquals(project.getId(), saved.getProject().getId());
  }

  @Test
  void shouldReturnDocumentsForProject() {
    Instant now = Instant.now();

    Organization organization = saveOrganization(now);
    Project project = saveProject(organization, now);

    Document firstDocument = new Document(
          UUID.randomUUID(),
          "Architecture Diagram",
          "architecture.pdf",
          "application/pdf",
          "documents/architecture.pdf",
          245760L,
          now,
          project
    );

    Document secondDocument = new Document(
          UUID.randomUUID(),
          "Project Brief",
          "brief.docx",
          "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
          "documents/brief.docx",
          128000L,
          now,
          project
    );

    documentRepository.saveAndFlush(firstDocument);
    documentRepository.saveAndFlush(secondDocument);

    List<Document> results =
          documentRepository.findByProjectId(project.getId());

    assertEquals(2, results.size());
    assertTrue(
          results.stream()
                .anyMatch(document ->
                      document.getName().equals("Architecture Diagram"))
    );
    assertTrue(
          results.stream()
                .anyMatch(document ->
                      document.getName().equals("Project Brief"))
    );
  }

  @Test
  void shouldReturnEmptyListWhenProjectHasNoDocuments() {
    Instant now = Instant.now();

    Organization organization = saveOrganization(now);
    Project project = saveProject(organization, now);

    List<Document> results =
          documentRepository.findByProjectId(project.getId());

    assertTrue(results.isEmpty());
  }

  private Organization saveOrganization(Instant now) {
    return organizationRepository.save(
          new Organization(
                UUID.randomUUID(),
                "Acme",
                now,
                now
          )
    );
  }

  private Project saveProject(
        Organization organization,
        Instant now
  ) {
    return projectRepository.save(
          new Project(
                UUID.randomUUID(),
                "Brand Engine",
                "Document repository test project",
                now,
                organization
          )
    );
  }
}