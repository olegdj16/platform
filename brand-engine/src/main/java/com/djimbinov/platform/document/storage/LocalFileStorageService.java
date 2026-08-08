package com.djimbinov.platform.document.storage;

import com.djimbinov.platform.document.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

  private final Path storageRoot;

  public LocalFileStorageService(
        @Value("${app.storage.location}") String storageLocation
  ) {
    this.storageRoot = Path.of(storageLocation)
          .toAbsolutePath()
          .normalize();

    try {
      Files.createDirectories(storageRoot);
    } catch (IOException exception) {
      throw new FileStorageException(
            "Could not initialize file storage.",
            exception
      );
    }
  }

  @Override
  public StoredFile store(MultipartFile file) {
    validateFile(file);

    String originalFilename = cleanFilename(
          file.getOriginalFilename()
    );

    String extension = getExtension(originalFilename);
    String storageKey = UUID.randomUUID() + extension;

    Path destination = storageRoot
          .resolve(storageKey)
          .normalize();

    verifyDestination(destination);

    try (InputStream inputStream = file.getInputStream()) {
      Files.copy(
            inputStream,
            destination,
            StandardCopyOption.REPLACE_EXISTING
      );
    } catch (IOException exception) {
      throw new FileStorageException(
            "Could not store file: " + originalFilename,
            exception
      );
    }

    return new StoredFile(
          originalFilename,
          file.getContentType(),
          storageKey,
          file.getSize()
    );
  }

  @Override
  public void delete(String storageKey) {
    if (!StringUtils.hasText(storageKey)) {
      return;
    }

    Path target = storageRoot
          .resolve(storageKey)
          .normalize();

    verifyDestination(target);

    try {
      Files.deleteIfExists(target);
    } catch (IOException exception) {
      throw new FileStorageException(
            "Could not delete stored file.",
            exception
      );
    }
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new FileStorageException(
            "File must not be empty."
      );
    }

    if (!StringUtils.hasText(file.getOriginalFilename())) {
      throw new FileStorageException(
            "File must have a valid filename."
      );
    }
  }

  private String cleanFilename(String filename) {
    String cleanedFilename =
          StringUtils.cleanPath(filename);

    if (cleanedFilename.contains("..")) {
      throw new FileStorageException(
            "Filename contains an invalid path sequence."
      );
    }

    return cleanedFilename;
  }

  private String getExtension(String filename) {
    int extensionIndex = filename.lastIndexOf('.');

    if (extensionIndex < 0) {
      return "";
    }

    return filename.substring(extensionIndex);
  }

  private void verifyDestination(Path destination) {
    if (!destination.startsWith(storageRoot)) {
      throw new FileStorageException(
            "Invalid storage destination."
      );
    }
  }

  @Override
  public Path resolve(String storageKey) {

    Path target = storageRoot
          .resolve(storageKey)
          .normalize();

    verifyDestination(target);

    return target;
  }
}