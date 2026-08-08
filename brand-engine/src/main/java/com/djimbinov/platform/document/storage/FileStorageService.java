package com.djimbinov.platform.document.storage;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;

public interface FileStorageService {

  StoredFile store(MultipartFile file);

  void delete(String storageKey);

  Path resolve(String storageKey);
}