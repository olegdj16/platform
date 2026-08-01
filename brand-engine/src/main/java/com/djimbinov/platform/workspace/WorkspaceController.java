package com.djimbinov.platform.workspace;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WorkspaceController {

  @GetMapping("/api/v1/workspace")
  public Map<String, Object> workspace() {
    return Map.of(
          "application", "Brand Engine",
          "version", "0.0.1",
          "status", "UP"
    );
  }
}