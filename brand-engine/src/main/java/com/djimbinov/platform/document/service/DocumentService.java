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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

  private final DocumentRepository documentRepository;
  private final ProjectRepository projectRepository;
  private final DocumentMapper documentMapper;

  public DocumentService(
        DocumentRepository documentRepository,
        ProjectRepository projectRepository,
        DocumentMapper documentMapper
  ) {
    this.documentRepository = documentRepository;
    this.projectRepository = projectRepository;
    this.documentMapper = documentMapper;
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
}