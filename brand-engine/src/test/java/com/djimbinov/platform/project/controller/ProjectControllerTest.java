package com.djimbinov.platform.project.controller;

import com.djimbinov.platform.auth.security.JwtAuthenticationFilter;
import com.djimbinov.platform.project.dto.ProjectRequest;
import com.djimbinov.platform.project.dto.ProjectResponse;
import com.djimbinov.platform.project.exception.ProjectNotFoundException;
import com.djimbinov.platform.project.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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

import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ProjectService projectService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  void shouldReturnProjectWhenProjectExists() throws Exception {
    UUID projectId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();
    Instant createdAt = Instant.now();

    ProjectResponse response = new ProjectResponse(
          projectId,
          organizationId,
          "Brand Engine",
          "Testing project controller",
          createdAt
    );

    when(projectService.findById(projectId))
          .thenReturn(response);

    mockMvc.perform(get("/api/v1/projects/{id}", projectId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id")
                .value(projectId.toString()))
          .andExpect(jsonPath("$.organizationId")
                .value(organizationId.toString()))
          .andExpect(jsonPath("$.name")
                .value("Brand Engine"))
          .andExpect(jsonPath("$.description")
                .value("Testing project controller"));

    verify(projectService).findById(projectId);
  }

  @Test
  void shouldReturnNotFoundWhenProjectDoesNotExist() throws Exception {
    UUID projectId = UUID.randomUUID();

    when(projectService.findById(projectId))
          .thenThrow(new ProjectNotFoundException(projectId));

    mockMvc.perform(get("/api/v1/projects/{id}", projectId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.message")
                .value("Project not found: " + projectId))
          .andExpect(jsonPath("$.data").isEmpty());

    verify(projectService).findById(projectId);
  }

  @Test
  void shouldReturnAllProjects() throws Exception {
    UUID firstProjectId = UUID.randomUUID();
    UUID secondProjectId = UUID.randomUUID();

    ProjectResponse firstResponse = new ProjectResponse(
          firstProjectId,
          UUID.randomUUID(),
          "Brand Engine",
          "First project",
          Instant.now()
    );

    ProjectResponse secondResponse = new ProjectResponse(
          secondProjectId,
          UUID.randomUUID(),
          "Second Project",
          "Second project",
          Instant.now()
    );

    when(projectService.findAll())
          .thenReturn(List.of(firstResponse, secondResponse));

    mockMvc.perform(get("/api/v1/projects"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message")
                .value("Projects loaded successfully"))
          .andExpect(jsonPath("$.data[0].id")
                .value(firstProjectId.toString()))
          .andExpect(jsonPath("$.data[0].name")
                .value("Brand Engine"))
          .andExpect(jsonPath("$.data[1].id")
                .value(secondProjectId.toString()))
          .andExpect(jsonPath("$.data[1].name")
                .value("Second Project"));

    verify(projectService).findAll();
  }

  @Test
  void shouldCreateProjectWhenRequestIsValid() throws Exception {
    UUID organizationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    Instant createdAt = Instant.now();

    ProjectRequest request = new ProjectRequest();
    request.setOrganizationId(organizationId);
    request.setName("Brand Engine");
    request.setDescription("Testing project creation");

    ProjectResponse response = new ProjectResponse(
          projectId,
          organizationId,
          request.getName(),
          request.getDescription(),
          createdAt
    );

    when(projectService.create(any(ProjectRequest.class)))
          .thenReturn(response);

    mockMvc.perform(
                post("/api/v1/projects")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message")
                .value("Project created successfully"))
          .andExpect(jsonPath("$.data.id")
                .value(projectId.toString()))
          .andExpect(jsonPath("$.data.organizationId")
                .value(organizationId.toString()))
          .andExpect(jsonPath("$.data.name")
                .value("Brand Engine"))
          .andExpect(jsonPath("$.data.description")
                .value("Testing project creation"));

    verify(projectService).create(
          argThat(actualRequest ->
                organizationId.equals(actualRequest.getOrganizationId())
                      && "Brand Engine".equals(actualRequest.getName())
                      && "Testing project creation"
                      .equals(actualRequest.getDescription())
          )
    );
  }

  @Test
  void shouldReturnBadRequestWhenProjectRequestIsInvalid() throws Exception {
    ProjectRequest request = new ProjectRequest();
    request.setOrganizationId(UUID.randomUUID());
    request.setName("");
    request.setDescription("Invalid project request");

    mockMvc.perform(
                post("/api/v1/projects")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.success").value(false));

    verifyNoInteractions(projectService);
  }

  @Test
  void shouldUpdateProjectWhenRequestIsValid() throws Exception {
    UUID projectId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();

    ProjectRequest request = new ProjectRequest();
    request.setOrganizationId(organizationId);
    request.setName("Updated Brand Engine");
    request.setDescription("Updated controller test");

    ProjectResponse response = new ProjectResponse(
          projectId,
          organizationId,
          request.getName(),
          request.getDescription(),
          Instant.now()
    );

    when(projectService.update(
          eq(projectId),
          any(ProjectRequest.class)
    )).thenReturn(response);

    mockMvc.perform(
                put("/api/v1/projects/{id}", projectId)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id")
                .value(projectId.toString()))
          .andExpect(jsonPath("$.organizationId")
                .value(organizationId.toString()))
          .andExpect(jsonPath("$.name")
                .value("Updated Brand Engine"))
          .andExpect(jsonPath("$.description")
                .value("Updated controller test"));

    verify(projectService).update(
          eq(projectId),
          argThat(actualRequest ->
                organizationId.equals(actualRequest.getOrganizationId())
                      && "Updated Brand Engine".equals(actualRequest.getName())
          )
    );
  }

  @Test
  void shouldDeleteProjectWhenProjectExists() throws Exception {
    UUID projectId = UUID.randomUUID();

    mockMvc.perform(delete("/api/v1/projects/{id}", projectId))
          .andExpect(status().isNoContent());

    verify(projectService).delete(projectId);
  }

  @Test
  void shouldReturnNotFoundWhenDeletingMissingProject() throws Exception {
    UUID projectId = UUID.randomUUID();

    doThrow(new ProjectNotFoundException(projectId))
          .when(projectService)
          .delete(projectId);

    mockMvc.perform(delete("/api/v1/projects/{id}", projectId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.message")
                .value("Project not found: " + projectId))
          .andExpect(jsonPath("$.data").isEmpty());

    verify(projectService).delete(projectId);
  }
}