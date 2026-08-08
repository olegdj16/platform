package com.djimbinov.platform.document.controller;

import com.djimbinov.platform.common.ApiResponse;
import com.djimbinov.platform.document.dto.DocumentRequest;
import com.djimbinov.platform.document.dto.DocumentResponse;
import com.djimbinov.platform.document.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.djimbinov.platform.document.dto.DocumentUploadRequest;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

  private final DocumentService documentService;

  public DocumentController(DocumentService documentService) {
    this.documentService = documentService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<DocumentResponse>> create(
        @Valid @RequestBody DocumentRequest request
  ) {

    DocumentResponse document =
          documentService.create(request);

    return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(ApiResponse.success(
                "Document created successfully",
                document
          ));
  }

  @GetMapping("/project/{projectId}")
  public ApiResponse<List<DocumentResponse>> findByProjectId(
        @PathVariable UUID projectId
  ) {

    return ApiResponse.success(
          "Documents loaded successfully",
          documentService.findByProjectId(projectId)
    );
  }

  @GetMapping("/{id}")
  public ApiResponse<DocumentResponse> findById(
        @PathVariable UUID id
  ) {
    return ApiResponse.success(
          "Document loaded successfully",
          documentService.findById(id)
    );
  }

  @PutMapping("/{id}")
  public ApiResponse<DocumentResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody DocumentRequest request
  ) {
    return ApiResponse.success(
          "Document updated successfully",
          documentService.update(id, request)
    );
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
        @PathVariable UUID id
  ) {
    documentService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(
        value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public ResponseEntity<ApiResponse<DocumentResponse>> upload(
        @RequestParam UUID projectId,
        @RequestParam String name,
        @RequestPart MultipartFile file
  ) {
    DocumentUploadRequest request = new DocumentUploadRequest(
          projectId,
          name,
          file
    );

    DocumentResponse document =
          documentService.upload(request);

    return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(ApiResponse.success(
                "Document uploaded successfully",
                document
          ));
  }

  @GetMapping("/{id}/text")
  public ApiResponse<String> extractText(
        @PathVariable UUID id
  ) {

    return ApiResponse.success(
          "Document text extracted successfully",
          documentService.extractText(id)
    );
  }
}