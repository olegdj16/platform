package com.djimbinov.platform.document.service;

import com.djimbinov.platform.document.dto.DocumentRequest;
import com.djimbinov.platform.document.dto.DocumentResponse;
import com.djimbinov.platform.document.exception.DocumentNotFoundException;
import com.djimbinov.platform.document.mapper.DocumentMapper;
import com.djimbinov.platform.document.model.Document;
import com.djimbinov.platform.document.repository.DocumentRepository;
import com.djimbinov.platform.document.storage.FileStorageService;
import com.djimbinov.platform.project.exception.ProjectNotFoundException;
import com.djimbinov.platform.project.model.Project;
import com.djimbinov.platform.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.djimbinov.platform.document.dto.DocumentUploadRequest;
import com.djimbinov.platform.document.storage.StoredFile;


import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

  private final DocumentRepository documentRepository;
  private final ProjectRepository projectRepository;
  private final DocumentMapper documentMapper;
  private final FileStorageService fileStorageService;
  private final PdfTextExtractionService pdfTextExtractionService;
  private final DocumentProcessingService documentProcessingService;

  public DocumentService(
        DocumentRepository documentRepository,
        ProjectRepository projectRepository,
        DocumentMapper documentMapper,
        FileStorageService fileStorageService,
        PdfTextExtractionService pdfTextExtractionService,
        DocumentProcessingService documentProcessingService
  ) {
    this.documentRepository = documentRepository;
    this.projectRepository = projectRepository;
    this.documentMapper = documentMapper;
    this.fileStorageService = fileStorageService;
    this.pdfTextExtractionService = pdfTextExtractionService;
    this.documentProcessingService = documentProcessingService;
  }

  @Transactional(readOnly = true)
  public String extractText(UUID documentId) {

    Document document =
          documentRepository.findById(documentId)
                .orElseThrow(() ->
                      new DocumentNotFoundException(documentId));

    return pdfTextExtractionService.extractText(
          fileStorageService.resolve(
                document.getStorageKey()
          )
    );
  }

  @Transactional
  public DocumentResponse create(DocumentRequest request) {

    Project project = projectRepository.findById(request.projectId())
          .orElseThrow(() ->
                new ProjectNotFoundException(request.projectId()));

    Document document =
          documentMapper.toModel(request, project);

    Document savedDocument =
          documentRepository.save(document);

    return documentMapper.toResponse(savedDocument);
  }

  @Transactional(readOnly = true)
  public List<DocumentResponse> findByProjectId(UUID projectId) {

    projectRepository.findById(projectId)
          .orElseThrow(() -> new ProjectNotFoundException(projectId));

    return documentRepository.findByProjectId(projectId)
          .stream()
          .map(documentMapper::toResponse)
          .toList();
  }

  @Transactional(readOnly = true)
  public DocumentResponse findById(UUID id) {
    Document document = documentRepository.findById(id)
          .orElseThrow(() -> new DocumentNotFoundException(id));

    return documentMapper.toResponse(document);
  }

  @Transactional
  public DocumentResponse update(UUID id, DocumentRequest request) {

    Document document = documentRepository.findById(id)
          .orElseThrow(() -> new DocumentNotFoundException(id));

    Project project = projectRepository.findById(request.projectId())
          .orElseThrow(() -> new ProjectNotFoundException(
                request.projectId()
          ));

    documentMapper.updateModel(document, request, project);

    Document updatedDocument =
          documentRepository.save(document);

    return documentMapper.toResponse(updatedDocument);
  }

  @Transactional
  public void delete(UUID id) {
    Document document = documentRepository.findById(id)
          .orElseThrow(() -> new DocumentNotFoundException(id));

    documentRepository.delete(document);
  }

  @Transactional
  public DocumentResponse upload(DocumentUploadRequest request) {

    Project project = projectRepository.findById(request.projectId())
          .orElseThrow(() ->
                new ProjectNotFoundException(request.projectId()));

    StoredFile storedFile =
          fileStorageService.store(request.file());

    DocumentRequest documentRequest = new DocumentRequest(
          request.projectId(),
          request.name(),
          storedFile.originalFilename(),
          storedFile.contentType(),
          storedFile.storageKey(),
          storedFile.sizeBytes()
    );

    try {
      Document document =
            documentMapper.toModel(
                  documentRequest,
                  project
            );

      Document savedDocument =
            documentRepository.save(document);

      Path filePath =
            fileStorageService.resolve(
                  savedDocument.getStorageKey()
            );

      documentProcessingService.process(
            savedDocument,
            filePath
      );

      return documentMapper.toResponse(
            savedDocument
      );

    } catch (RuntimeException exception) {
      fileStorageService.delete(
            storedFile.storageKey()
      );

      throw exception;
    }
  }
}