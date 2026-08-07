package com.djimbinov.platform.ai.controller;

import com.djimbinov.platform.auth.security.JwtAuthenticationFilter;
import com.djimbinov.platform.ai.service.AIChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AIChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class AIChatControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AIChatService aiChatService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  void chatShouldReturnAiResponse() throws Exception {
    String message = "Hello AI";
    String response = "AI response for: Hello AI";

    when(aiChatService.chat(message))
          .thenReturn(response);

    mockMvc.perform(
                post("/api/v1/ai/chat")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content("""
                            {
                              "message": "Hello AI"
                            }
                            """)
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.response").value(response));

    verify(aiChatService).chat(message);
  }

  @Test
  void chatShouldReturnBadRequestWhenMessageIsBlank() throws Exception {
    mockMvc.perform(
                post("/api/v1/ai/chat")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content("""
                            {
                              "message": ""
                            }
                            """)
          )
          .andExpect(status().isBadRequest());

    verifyNoInteractions(aiChatService);
  }

  @Test
  void chatShouldReturnBadRequestWhenMessageIsMissing() throws Exception {
    mockMvc.perform(
                post("/api/v1/ai/chat")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content("""
                            {
                            }
                            """)
          )
          .andExpect(status().isBadRequest());

    verifyNoInteractions(aiChatService);
  }
}