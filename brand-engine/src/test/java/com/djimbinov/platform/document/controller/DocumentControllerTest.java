package com.djimbinov.platform.document.controller;

import com.djimbinov.platform.auth.security.JwtAuthenticationFilter;
import com.djimbinov.platform.document.dto.DocumentResponse;
import com.djimbinov.platform.document.dto.DocumentUploadRequest;
import com.djimbinov.platform.document.exception.DocumentNotFoundException;
import com.djimbinov.platform.document.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import org.springframework.mock.web.MockMultipartFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.mockito.ArgumentMatchers.argThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.djimbinov.platform.document.dto.DocumentRequest;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;


@WebMvcTest(DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DocumentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private DocumentService documentService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  void shouldReturnDocumentWhenDocumentExists() throws Exception {
    UUID documentId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    Instant createdAt = Instant.now();

    DocumentResponse response = new DocumentResponse(
          documentId,
          projectId,
          "Architecture Diagram",
          "architecture.pdf",
          "application/pdf",
          "documents/architecture.pdf",
          245760L,
          createdAt
    );

    when(documentService.findById(documentId))
          .thenReturn(response);

    mockMvc.perform(get("/api/v1/documents/{id}", documentId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message")
                .value("Document loaded successfully"))
          .andExpect(jsonPath("$.data.id")
                .value(documentId.toString()))
          .andExpect(jsonPath("$.data.projectId")
                .value(projectId.toString()))
          .andExpect(jsonPath("$.data.name")
                .value("Architecture Diagram"))
          .andExpect(jsonPath("$.data.originalFilename")
                .value("architecture.pdf"))
          .andExpect(jsonPath("$.data.contentType")
                .value("application/pdf"))
          .andExpect(jsonPath("$.data.storageKey")
                .value("documents/architecture.pdf"))
          .andExpect(jsonPath("$.data.sizeBytes")
                .value(245760));

    verify(documentService).findById(documentId);
  }

  @Test
  void shouldReturnNotFoundWhenDocumentDoesNotExist() throws Exception {
    UUID documentId = UUID.randomUUID();

    when(documentService.findById(documentId))
          .thenThrow(new DocumentNotFoundException(documentId));

    mockMvc.perform(get("/api/v1/documents/{id}", documentId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.message")
                .value("Document not found: " + documentId))
          .andExpect(jsonPath("$.data").isEmpty());

    verify(documentService).findById(documentId);
  }

  @Test
  void shouldReturnDocumentsForProject() throws Exception {
    UUID projectId = UUID.randomUUID();

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

    when(documentService.findByProjectId(projectId))
          .thenReturn(List.of(firstResponse, secondResponse));

    mockMvc.perform(
                get("/api/v1/documents/project/{projectId}", projectId)
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message")
                .value("Documents loaded successfully"))
          .andExpect(jsonPath("$.data.length()").value(2))
          .andExpect(jsonPath("$.data[0].projectId")
                .value(projectId.toString()))
          .andExpect(jsonPath("$.data[0].name")
                .value("Architecture Diagram"))
          .andExpect(jsonPath("$.data[1].projectId")
                .value(projectId.toString()))
          .andExpect(jsonPath("$.data[1].name")
                .value("Project Brief"));

    verify(documentService).findByProjectId(projectId);
  }

  @Test
  void shouldCreateDocumentWhenRequestIsValid() throws Exception {
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

    DocumentResponse response = new DocumentResponse(
          documentId,
          projectId,
          request.name(),
          request.originalFilename(),
          request.contentType(),
          request.storageKey(),
          request.sizeBytes(),
          createdAt
    );

    when(documentService.create(any(DocumentRequest.class)))
          .thenReturn(response);

    mockMvc.perform(
                post("/api/v1/documents")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message")
                .value("Document created successfully"))
          .andExpect(jsonPath("$.data.id")
                .value(documentId.toString()))
          .andExpect(jsonPath("$.data.projectId")
                .value(projectId.toString()))
          .andExpect(jsonPath("$.data.name")
                .value("Architecture Diagram"))
          .andExpect(jsonPath("$.data.originalFilename")
                .value("architecture.pdf"))
          .andExpect(jsonPath("$.data.contentType")
                .value("application/pdf"))
          .andExpect(jsonPath("$.data.storageKey")
                .value("documents/architecture.pdf"))
          .andExpect(jsonPath("$.data.sizeBytes")
                .value(245760));

    verify(documentService).create(
          argThat(actualRequest ->
                projectId.equals(actualRequest.projectId())
                      && "Architecture Diagram".equals(actualRequest.name())
                      && "architecture.pdf"
                      .equals(actualRequest.originalFilename())
                      && "application/pdf"
                      .equals(actualRequest.contentType())
                      && "documents/architecture.pdf"
                      .equals(actualRequest.storageKey())
                      && Long.valueOf(245760L)
                      .equals(actualRequest.sizeBytes())
          )
    );
  }

  @Test
  void shouldReturnBadRequestWhenDocumentRequestIsInvalid() throws Exception {
    DocumentRequest request = new DocumentRequest(
          UUID.randomUUID(),
          "",
          "",
          "application/pdf",
          "documents/architecture.pdf",
          245760L
    );

    mockMvc.perform(
                post("/api/v1/documents")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.success").value(false));

    verifyNoInteractions(documentService);
  }

  @Test
  void shouldUpdateDocumentWhenRequestIsValid() throws Exception {
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

    DocumentResponse response = new DocumentResponse(
          documentId,
          projectId,
          request.name(),
          request.originalFilename(),
          request.contentType(),
          request.storageKey(),
          request.sizeBytes(),
          createdAt
    );

    when(documentService.update(
          eq(documentId),
          any(DocumentRequest.class)
    )).thenReturn(response);

    mockMvc.perform(
                put("/api/v1/documents/{id}", documentId)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message")
                .value("Document updated successfully"))
          .andExpect(jsonPath("$.data.id")
                .value(documentId.toString()))
          .andExpect(jsonPath("$.data.projectId")
                .value(projectId.toString()))
          .andExpect(jsonPath("$.data.name")
                .value("Updated Architecture Diagram"))
          .andExpect(jsonPath("$.data.originalFilename")
                .value("architecture.pdf"))
          .andExpect(jsonPath("$.data.contentType")
                .value("application/pdf"))
          .andExpect(jsonPath("$.data.storageKey")
                .value("documents/architecture.pdf"))
          .andExpect(jsonPath("$.data.sizeBytes")
                .value(245760));

    verify(documentService).update(
          eq(documentId),
          argThat(actualRequest ->
                projectId.equals(actualRequest.projectId())
                      && "Updated Architecture Diagram"
                      .equals(actualRequest.name())
                      && "architecture.pdf"
                      .equals(actualRequest.originalFilename())
          )
    );
  }

  @Test
  void shouldDeleteDocumentWhenDocumentExists() throws Exception {
    UUID documentId = UUID.randomUUID();

    mockMvc.perform(delete("/api/v1/documents/{id}", documentId))
          .andExpect(status().isNoContent());

    verify(documentService).delete(documentId);
  }

  @Test
  void shouldReturnNotFoundWhenDeletingMissingDocument() throws Exception {
    UUID documentId = UUID.randomUUID();

    doThrow(new DocumentNotFoundException(documentId))
          .when(documentService)
          .delete(documentId);

    mockMvc.perform(delete("/api/v1/documents/{id}", documentId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.message")
                .value("Document not found: " + documentId))
          .andExpect(jsonPath("$.data").isEmpty());

    verify(documentService).delete(documentId);
  }

  @Test
  void shouldUploadDocumentWhenRequestIsValid() throws Exception {
    UUID projectId = UUID.randomUUID();
    UUID documentId = UUID.randomUUID();
    Instant createdAt = Instant.now();

    MockMultipartFile file = new MockMultipartFile(
          "file",
          "architecture.pdf",
          "application/pdf",
          "pdf-content".getBytes()
    );

    DocumentResponse response = new DocumentResponse(
          documentId,
          projectId,
          "Architecture Diagram",
          "architecture.pdf",
          "application/pdf",
          "generated-file-key.pdf",
          file.getSize(),
          createdAt
    );

    when(documentService.upload(any(DocumentUploadRequest.class)))
          .thenReturn(response);

    mockMvc.perform(
                multipart("/api/v1/documents/upload")
                      .file(file)
                      .param("projectId", projectId.toString())
                      .param("name", "Architecture Diagram")
          )
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message")
                .value("Document uploaded successfully"))
          .andExpect(jsonPath("$.data.projectId")
                .value(projectId.toString()))
          .andExpect(jsonPath("$.data.name")
                .value("Architecture Diagram"))
          .andExpect(jsonPath("$.data.originalFilename")
                .value("architecture.pdf"))
          .andExpect(jsonPath("$.data.storageKey")
                .value("generated-file-key.pdf"));

    verify(documentService).upload(
          argThat(request ->
                projectId.equals(request.projectId())
                      && "Architecture Diagram".equals(request.name())
                      && request.file() != null
          )
    );
  }
}