package com.djimbinov.platform.document.mapper;

import com.djimbinov.platform.document.dto.DocumentRequest;
import com.djimbinov.platform.document.dto.DocumentResponse;
import com.djimbinov.platform.document.model.Document;
import com.djimbinov.platform.project.model.Project;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class DocumentMapper {

  public Document toModel(
        DocumentRequest request,
        Project project
  ) {
    return new Document(
          UUID.randomUUID(),
          request.name(),
          request.originalFilename(),
          request.contentType(),
          request.storageKey(),
          request.sizeBytes(),
          Instant.now(),
          project
    );
  }

  public DocumentResponse toResponse(Document document) {
    return new DocumentResponse(
          document.getId(),
          document.getProject().getId(),
          document.getName(),
          document.getOriginalFilename(),
          document.getContentType(),
          document.getStorageKey(),
          document.getSizeBytes(),
          document.getCreatedAt()
    );
  }

  public void updateModel(
        Document document,
        DocumentRequest request,
        Project project
  ) {
    document.setName(request.name());
    document.setProject(project);
  }
}