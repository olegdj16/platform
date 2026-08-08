package com.djimbinov.platform.ai.controller;

import com.djimbinov.platform.ai.service.DocumentAIService;
import com.djimbinov.platform.auth.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentAIController.class)
@AutoConfigureMockMvc(addFilters = false)
class DocumentAIControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private DocumentAIService documentAIService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  void askShouldReturnAiResponse() throws Exception {

    UUID projectId = UUID.randomUUID();

    when(documentAIService.ask(
          projectId,
          "What technologies does Brand Engine use?"
    )).thenReturn(
          "Brand Engine uses Java 21 and Spring Boot."
    );

    mockMvc.perform(
                post(
                      "/api/v1/ai/projects/{projectId}/ask",
                      projectId
                )
                      .contentType(MediaType.APPLICATION_JSON)
                      .content("""
                            {
                              "question": "What technologies does Brand Engine use?"
                            }
                            """)
          )
          .andExpect(status().isOk())
          .andExpect(
                jsonPath("$.response")
                      .value(
                            "Brand Engine uses Java 21 and Spring Boot."
                      )
          );

    verify(documentAIService).ask(
          projectId,
          "What technologies does Brand Engine use?"
    );
  }

  @Test
  void askShouldReturnBadRequestWhenQuestionIsBlank()
        throws Exception {

    UUID projectId = UUID.randomUUID();

    mockMvc.perform(
                post(
                      "/api/v1/ai/projects/{projectId}/ask",
                      projectId
                )
                      .contentType(MediaType.APPLICATION_JSON)
                      .content("""
                            {
                              "question": ""
                            }
                            """)
          )
          .andExpect(status().isBadRequest());

    verifyNoInteractions(documentAIService);
  }
}