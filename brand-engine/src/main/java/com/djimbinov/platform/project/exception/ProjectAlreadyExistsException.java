package com.djimbinov.platform.project.exception;

public class ProjectAlreadyExistsException extends RuntimeException {

  public ProjectAlreadyExistsException(String projectName) {
    super("A project named '" + projectName
          + "' already exists in this organization.");
  }
}