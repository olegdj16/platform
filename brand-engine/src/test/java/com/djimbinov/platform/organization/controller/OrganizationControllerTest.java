package com.djimbinov.platform.organization.controller;

import com.djimbinov.platform.organization.dto.OrganizationRequest;
import com.djimbinov.platform.organization.dto.OrganizationResponse;
import com.djimbinov.platform.organization.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrganizationController.class)
class OrganizationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private OrganizationService organizationService;

  @Test
  void shouldCreateOrganizationWhenRequestIsValid() throws Exception {
    UUID organizationId = UUID.randomUUID();

    OrganizationRequest request =
          new OrganizationRequest("Acme");

    OrganizationResponse response =
          new OrganizationResponse(
                organizationId,
                "Acme"
          );

    when(organizationService.create(any(OrganizationRequest.class)))
          .thenReturn(response);

    mockMvc.perform(
                post("/api/v1/organizations")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message")
                .value("Organization created successfully"))
          .andExpect(jsonPath("$.data.id")
                .value(organizationId.toString()))
          .andExpect(jsonPath("$.data.name")
                .value("Acme"));

    verify(organizationService).create(
          argThat(actualRequest ->
                "Acme".equals(actualRequest.getName())
          )
    );
  }

  @Test
  void shouldReturnAllOrganizations() throws Exception {
    OrganizationResponse firstResponse =
          new OrganizationResponse(
                UUID.randomUUID(),
                "Acme"
          );

    OrganizationResponse secondResponse =
          new OrganizationResponse(
                UUID.randomUUID(),
                "Globex"
          );

    when(organizationService.findAll())
          .thenReturn(List.of(
                firstResponse,
                secondResponse
          ));

    mockMvc.perform(get("/api/v1/organizations"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message")
                .value("Organizations loaded successfully"))
          .andExpect(jsonPath("$.data.length()").value(2))
          .andExpect(jsonPath("$.data[0].name")
                .value("Acme"))
          .andExpect(jsonPath("$.data[1].name")
                .value("Globex"));

    verify(organizationService).findAll();
  }
}