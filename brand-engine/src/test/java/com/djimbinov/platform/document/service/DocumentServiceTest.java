package com.djimbinov.platform.document.service;

import com.djimbinov.platform.document.dto.DocumentRequest;
import com.djimbinov.platform.document.dto.DocumentResponse;
import com.djimbinov.platform.document.exception.DocumentNotFoundException;
import com.djimbinov.platform.document.mapper.DocumentMapper;
import com.djimbinov.platform.document.model.Document;
import com.djimbinov.platform.document.repository.DocumentRepository;
import com.djimbinov.platform.project.exception.ProjectNotFoundException;
import com.djimbinov.platform.project.model.Project;
import com.djimbinov.platform.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.djimbinov.platform.document.storage.FileStorageService;
import com.djimbinov.platform.document.dto.DocumentUploadRequest;
import com.djimbinov.platform.document.storage.StoredFile;
import org.springframework.mock.web.MockMultipartFile;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

  @Mock
  private FileStorageService fileStorageService;

  @Mock
  private DocumentRepository documentRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private DocumentMapper documentMapper;

  private DocumentService documentService;

  @BeforeEach
  void setUp() {
    documentService = new DocumentService(
          documentRepository,
          projectRepository,
          documentMapper,
          fileStorageService
    );
  }

  @Test
  void shouldCreateDocumentWhenRequestIsValid() {
    UUID projectId = UUID.randomUUID();
    UUID documentId = UUID.randomUUID();
    Instant createdAt = Instant.now();

    DocumentRequest request = new DocumentRequest(
          projectId,
          "Architecture Diagram",
          "architecture.pdf",
          "application/pdf",
          "documents/architecture.pdf",
          245760L
    );

    Project project = mock(Project.class);
    Document document = mock(Document.class);
    Document savedDocument = mock(Document.class);

    DocumentResponse expectedResponse = new DocumentResponse(
          documentId,
          projectId,
          request.name(),
          request.originalFilename(),
          request.contentType(),
          request.storageKey(),
          request.sizeBytes(),
          createdAt
    );

    when(projectRepository.findById(projectId))
          .thenReturn(Optional.of(project));

    when(documentMapper.toModel(request, project))
          .thenReturn(document);

    when(documentRepository.save(document))
          .thenReturn(savedDocument);

    when(documentMapper.toResponse(savedDocument))
          .thenReturn(expectedResponse);

    DocumentResponse result = documentService.create(request);

    assertSame(expectedResponse, result);
    assertEquals(documentId, result.id());
    assertEquals(projectId, result.projectId());
    assertEquals("Architecture Diagram", result.name());

    verify(projectRepository).findById(projectId);
    verify(documentMapper).toModel(request, project);
    verify(documentRepository).save(document);
    verify(documentMapper).toResponse(savedDocument);
  }

  @Test
  void shouldThrowProjectNotFoundExceptionWhenCreatingDocument() {
    UUID projectId = UUID.randomUUID();

    DocumentRequest request = new DocumentRequest(
          projectId,
          "Architecture Diagram",
          "architecture.pdf",
          "application/pdf",
          "documents/architecture.pdf",
          245760L
    );

    when(projectRepository.findById(projectId))
          .thenReturn(Optional.empty());

    ProjectNotFoundException exception = assertThrows(
          ProjectNotFoundException.class,
          () -> documentService.create(request)
    );

    assertEquals(
          "Project not found: " + projectId,
          exception.getMessage()
    );

    verify(projectRepository).findById(projectId);
    verifyNoInteractions(documentRepository);
    verifyNoInteractions(documentMapper);
  }

  @Test
  void shouldReturnDocumentWhenDocumentExists() {
    UUID documentId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    Instant createdAt = Instant.now();

    Document document = mock(Document.class);

    DocumentResponse expectedResponse = new DocumentResponse(
          documentId,
          projectId,
          "Architecture Diagram",
          "architecture.pdf",
          "application/pdf",
          "documents/architecture.pdf",
          245760L,
          createdAt
    );

    when(documentRepository.findById(documentId))
          .thenReturn(Optional.of(document));

    when(documentMapper.toResponse(document))
          .thenReturn(expectedResponse);

    DocumentResponse result = documentService.findById(documentId);

    assertSame(expectedResponse, result);
    assertEquals(documentId, result.id());
    assertEquals(projectId, result.projectId());
    assertEquals("Architecture Diagram", result.name());

    verify(documentRepository).findById(documentId);
    verify(documentMapper).toResponse(document);
  }

  @Test
  void shouldThrowDocumentNotFoundExceptionWhenDocumentDoesNotExist() {
    UUID documentId = UUID.randomUUID();

    when(documentRepository.findById(documentId))
          .thenReturn(Optional.empty());

    DocumentNotFoundException exception = assertThrows(
          DocumentNotFoundException.class,
          () -> documentService.findById(documentId)
    );

    assertEquals(
          "Document not found: " + documentId,
          exception.getMessage()
    );

    verify(documentRepository).findById(documentId);
    verifyNoInteractions(documentMapper);
  }

  @Test
  void shouldReturnDocumentsForProjectWhenProjectExists() {
    UUID projectId = UUID.randomUUID();

    Project project = mock(Project.class);
    Document firstDocument = mock(Document.class);
    Document secondDocument = mock(Document.class);

    DocumentResponse firstResponse = new DocumentResponse(
          UUID.randomUUID(),
          projectId,
          "Architecture Diagram",
          "architecture.pdf",
          "application/pdf",
          "documents/architecture.pdf",
          245760L,
          Instant.now()
    );

    DocumentResponse secondResponse = new DocumentResponse(
          UUID.randomUUID(),
          projectId,
          "Project Brief",
          "brief.docx",
          "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
          "documents/brief.docx",
          128000L,
          Instant.now()
    );

    when(projectRepository.findById(projectId))
          .thenReturn(Optional.of(project));

    when(documentRepository.findByProjectId(projectId))
          .thenReturn(List.of(firstDocument, secondDocument));

    when(documentMapper.toResponse(firstDocument))
          .thenReturn(firstResponse);

    when(documentMapper.toResponse(secondDocument))
          .thenReturn(secondResponse);

    List<DocumentResponse> results =
          documentService.findByProjectId(projectId);

    assertEquals(2, results.size());
    assertEquals("Architecture Diagram", results.get(0).name());
    assertEquals("Project Brief", results.get(1).name());

    verify(projectRepository).findById(projectId);
    verify(documentRepository).findByProjectId(projectId);
    verify(documentMapper).toResponse(firstDocument);
    verify(documentMapper).toResponse(secondDocument);
  }

  @Test
  void shouldThrowProjectNotFoundExceptionWhenFindingDocumentsByProject() {
    UUID projectId = UUID.randomUUID();

    when(projectRepository.findById(projectId))
          .thenReturn(Optional.empty());

    ProjectNotFoundException exception = assertThrows(
          ProjectNotFoundException.class,
          () -> documentService.findByProjectId(projectId)
    );

    assertEquals(
          "Project not found: " + projectId,
          exception.getMessage()
    );

    verify(projectRepository).findById(projectId);
    verifyNoInteractions(documentRepository);
    verifyNoInteractions(documentMapper);
  }

  @Test
  void shouldUpdateDocumentWhenRequestIsValid() {
    UUID documentId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    Instant createdAt = Instant.now();

    DocumentRequest request = new DocumentRequest(
          projectId,
          "Updated Architecture Diagram",
          "architecture.pdf",
          "application/pdf",
          "documents/architecture.pdf",
          245760L
    );

    Document existingDocument = mock(Document.class);
    Document updatedDocument = mock(Document.class);
    Project project = mock(Project.class);

    DocumentResponse expectedResponse = new DocumentResponse(
          documentId,
          projectId,
          request.name(),
          request.originalFilename(),
          request.contentType(),
          request.storageKey(),
          request.sizeBytes(),
          createdAt
    );

    when(documentRepository.findById(documentId))
          .thenReturn(Optional.of(existingDocument));

    when(projectRepository.findById(projectId))
          .thenReturn(Optional.of(project));

    when(documentRepository.save(existingDocument))
          .thenReturn(updatedDocument);

    when(documentMapper.toResponse(updatedDocument))
          .thenReturn(expectedResponse);

    DocumentResponse result =
          documentService.update(documentId, request);

    assertSame(expectedResponse, result);
    assertEquals(documentId, result.id());
    assertEquals(projectId, result.projectId());
    assertEquals("Updated Architecture Diagram", result.name());

    verify(documentRepository).findById(documentId);
    verify(projectRepository).findById(projectId);
    verify(documentMapper)
          .updateModel(existingDocument, request, project);
    verify(documentRepository).save(existingDocument);
    verify(documentMapper).toResponse(updatedDocument);
  }

  @Test
  void shouldThrowDocumentNotFoundExceptionWhenUpdatingMissingDocument() {
    UUID documentId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();

    DocumentRequest request = new DocumentRequest(
          projectId,
          "Updated Architecture Diagram",
          "architecture.pdf",
          "application/pdf",
          "documents/architecture.pdf",
          245760L
    );

    when(documentRepository.findById(documentId))
          .thenReturn(Optional.empty());

    DocumentNotFoundException exception = assertThrows(
          DocumentNotFoundException.class,
          () -> documentService.update(documentId, request)
    );

    assertEquals(
          "Document not found: " + documentId,
          exception.getMessage()
    );

    verify(documentRepository).findById(documentId);
    verifyNoInteractions(projectRepository);
    verifyNoInteractions(documentMapper);
    verify(documentRepository, never()).save(any());
  }

  @Test
  void shouldThrowProjectNotFoundExceptionWhenUpdatingDocument() {
    UUID documentId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();

    DocumentRequest request = new DocumentRequest(
          projectId,
          "Updated Architecture Diagram",
          "architecture.pdf",
          "application/pdf",
          "documents/architecture.pdf",
          245760L
    );

    Document existingDocument = mock(Document.class);

    when(documentRepository.findById(documentId))
          .thenReturn(Optional.of(existingDocument));

    when(projectRepository.findById(projectId))
          .thenReturn(Optional.empty());

    ProjectNotFoundException exception = assertThrows(
          ProjectNotFoundException.class,
          () -> documentService.update(documentId, request)
    );

    assertEquals(
          "Project not found: " + projectId,
          exception.getMessage()
    );

    verify(documentRepository).findById(documentId);
    verify(projectRepository).findById(projectId);
    verify(documentMapper, never())
          .updateModel(any(), any(), any());
    verify(documentRepository, never()).save(any());
    verify(documentMapper, never()).toResponse(any());
  }

  @Test
  void shouldDeleteDocumentWhenDocumentExists() {
    UUID documentId = UUID.randomUUID();
    Document document = mock(Document.class);

    when(documentRepository.findById(documentId))
          .thenReturn(Optional.of(document));

    documentService.delete(documentId);

    verify(documentRepository).findById(documentId);
    verify(documentRepository).delete(document);
    verifyNoInteractions(projectRepository);
    verifyNoInteractions(documentMapper);
  }

  @Test
  void shouldThrowDocumentNotFoundExceptionWhenDeletingMissingDocument() {
    UUID documentId = UUID.randomUUID();

    when(documentRepository.findById(documentId))
          .thenReturn(Optional.empty());

    DocumentNotFoundException exception = assertThrows(
          DocumentNotFoundException.class,
          () -> documentService.delete(documentId)
    );

    assertEquals(
          "Document not found: " + documentId,
          exception.getMessage()
    );

    verify(documentRepository).findById(documentId);
    verify(documentRepository, never()).delete(any());
    verifyNoInteractions(projectRepository);
    verifyNoInteractions(documentMapper);
  }

  @Test
  void shouldUploadDocumentWhenRequestIsValid() {
    UUID projectId = UUID.randomUUID();
    UUID documentId = UUID.randomUUID();
    Instant createdAt = Instant.now();

    MockMultipartFile file = new MockMultipartFile(
          "file",
          "architecture.pdf",
          "application/pdf",
          "pdf-content".getBytes()
    );

    DocumentUploadRequest request = new DocumentUploadRequest(
          projectId,
          "Architecture Diagram",
          file
    );

    Project project = mock(Project.class);
    Document document = mock(Document.class);
    Document savedDocument = mock(Document.class);

    StoredFile storedFile = new StoredFile(
          "architecture.pdf",
          "application/pdf",
          "generated-file-key.pdf",
          file.getSize()
    );

    DocumentResponse expectedResponse = new DocumentResponse(
          documentId,
          projectId,
          request.name(),
          storedFile.originalFilename(),
          storedFile.contentType(),
          storedFile.storageKey(),
          storedFile.sizeBytes(),
          createdAt
    );

    when(projectRepository.findById(projectId))
          .thenReturn(Optional.of(project));

    when(fileStorageService.store(file))
          .thenReturn(storedFile);

    when(documentMapper.toModel(
          any(DocumentRequest.class),
          eq(project)
    )).thenReturn(document);

    when(documentRepository.save(document))
          .thenReturn(savedDocument);

    when(documentMapper.toResponse(savedDocument))
          .thenReturn(expectedResponse);

    DocumentResponse result = documentService.upload(request);

    assertSame(expectedResponse, result);
    assertEquals(documentId, result.id());
    assertEquals("generated-file-key.pdf", result.storageKey());

    verify(projectRepository).findById(projectId);
    verify(fileStorageService).store(file);

    verify(documentMapper).toModel(
          argThat(actualRequest ->
                projectId.equals(actualRequest.projectId())
                      && "Architecture Diagram"
                      .equals(actualRequest.name())
                      && "architecture.pdf"
                      .equals(actualRequest.originalFilename())
                      && "application/pdf"
                      .equals(actualRequest.contentType())
                      && "generated-file-key.pdf"
                      .equals(actualRequest.storageKey())
                      && Long.valueOf(file.getSize())
                      .equals(actualRequest.sizeBytes())
          ),
          eq(project)
    );

    verify(documentRepository).save(document);
    verify(documentMapper).toResponse(savedDocument);
    verify(fileStorageService, never()).delete(any());
  }

  @Test
  void shouldThrowProjectNotFoundExceptionBeforeStoringFile() {
    UUID projectId = UUID.randomUUID();

    MockMultipartFile file = new MockMultipartFile(
          "file",
          "architecture.pdf",
          "application/pdf",
          "pdf-content".getBytes()
    );

    DocumentUploadRequest request = new DocumentUploadRequest(
          projectId,
          "Architecture Diagram",
          file
    );

    when(projectRepository.findById(projectId))
          .thenReturn(Optional.empty());

    ProjectNotFoundException exception = assertThrows(
          ProjectNotFoundException.class,
          () -> documentService.upload(request)
    );

    assertEquals(
          "Project not found: " + projectId,
          exception.getMessage()
    );

    verify(projectRepository).findById(projectId);
    verifyNoInteractions(fileStorageService);
    verifyNoInteractions(documentRepository);
    verifyNoInteractions(documentMapper);
  }

  @Test
  void shouldDeleteStoredFileWhenDatabaseSaveFails() {
    UUID projectId = UUID.randomUUID();

    MockMultipartFile file = new MockMultipartFile(
          "file",
          "architecture.pdf",
          "application/pdf",
          "pdf-content".getBytes()
    );

    DocumentUploadRequest request = new DocumentUploadRequest(
          projectId,
          "Architecture Diagram",
          file
    );

    Project project = mock(Project.class);
    Document document = mock(Document.class);

    StoredFile storedFile = new StoredFile(
          "architecture.pdf",
          "application/pdf",
          "generated-file-key.pdf",
          file.getSize()
    );

    when(projectRepository.findById(projectId))
          .thenReturn(Optional.of(project));

    when(fileStorageService.store(file))
          .thenReturn(storedFile);

    when(documentMapper.toModel(
          any(DocumentRequest.class),
          eq(project)
    )).thenReturn(document);

    RuntimeException databaseException =
          new RuntimeException("Database failure");

    when(documentRepository.save(document))
          .thenThrow(databaseException);

    RuntimeException exception = assertThrows(
          RuntimeException.class,
          () -> documentService.upload(request)
    );

    assertSame(databaseException, exception);

    verify(fileStorageService)
          .delete("generated-file-key.pdf");

    verify(documentMapper, never()).toResponse(any());
  }
}