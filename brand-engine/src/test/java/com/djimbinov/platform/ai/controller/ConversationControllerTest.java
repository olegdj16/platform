package com.djimbinov.platform.ai.controller;

import com.djimbinov.platform.ai.dto.ConversationRequest;
import com.djimbinov.platform.ai.dto.ConversationResponse;
import com.djimbinov.platform.ai.exception.ConversationNotFoundException;
import com.djimbinov.platform.ai.service.ConversationService;
import com.djimbinov.platform.auth.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.djimbinov.platform.ai.service.ConversationAIService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConversationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConversationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ConversationService conversationService;

  @MockitoBean
  private ConversationAIService conversationAIService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  void createShouldReturnCreatedConversation() throws Exception {

    UUID id = UUID.randomUUID();
    Instant createdAt = Instant.now();

    ConversationResponse response =
          new ConversationResponse(
                id,
                "Brand Engine Discussion",
                createdAt
          );

    when(conversationService.createConversation(
          new ConversationRequest("Brand Engine Discussion")
    )).thenReturn(response);

    mockMvc.perform(
                post("/api/v1/ai/conversations")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content("""
                            {
                              "title": "Brand Engine Discussion"
                            }
                            """)
          )
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(
                jsonPath("$.message")
                      .value("Conversation created successfully")
          )
          .andExpect(jsonPath("$.data.id").value(id.toString()))
          .andExpect(
                jsonPath("$.data.title")
                      .value("Brand Engine Discussion")
          );

    verify(conversationService).createConversation(
          new ConversationRequest("Brand Engine Discussion")
    );
  }

  @Test
  void createShouldReturnBadRequestWhenTitleIsBlank() throws Exception {

    mockMvc.perform(
                post("/api/v1/ai/conversations")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content("""
                            {
                              "title": ""
                            }
                            """)
          )
          .andExpect(status().isBadRequest());

    verifyNoInteractions(conversationService);
  }

  @Test
  void findByIdShouldReturnConversation() throws Exception {

    UUID id = UUID.randomUUID();

    ConversationResponse response =
          new ConversationResponse(
                id,
                "AI Discussion",
                Instant.now()
          );

    when(conversationService.getConversation(id))
          .thenReturn(response);

    mockMvc.perform(
                get("/api/v1/ai/conversations/{id}", id)
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.id").value(id.toString()))
          .andExpect(
                jsonPath("$.data.title")
                      .value("AI Discussion")
          );

    verify(conversationService).getConversation(id);
  }

  @Test
  void findAllShouldReturnConversations() throws Exception {

    ConversationResponse first =
          new ConversationResponse(
                UUID.randomUUID(),
                "First Conversation",
                Instant.now()
          );

    ConversationResponse second =
          new ConversationResponse(
                UUID.randomUUID(),
                "Second Conversation",
                Instant.now()
          );

    when(conversationService.getConversations())
          .thenReturn(List.of(first, second));

    mockMvc.perform(
                get("/api/v1/ai/conversations")
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(2))
          .andExpect(
                jsonPath("$.data[0].title")
                      .value("First Conversation")
          )
          .andExpect(
                jsonPath("$.data[1].title")
                      .value("Second Conversation")
          );

    verify(conversationService).getConversations();
  }

  @Test
  void deleteShouldReturnNoContent() throws Exception {

    UUID id = UUID.randomUUID();

    mockMvc.perform(
                delete("/api/v1/ai/conversations/{id}", id)
          )
          .andExpect(status().isNoContent());

    verify(conversationService).deleteConversation(id);
  }

  @Test
  void findByIdShouldReturnNotFoundWhenConversationDoesNotExist()
        throws Exception {

    UUID id = UUID.randomUUID();

    when(conversationService.getConversation(id))
          .thenThrow(new ConversationNotFoundException(id));

    mockMvc.perform(
                get("/api/v1/ai/conversations/{id}", id)
          )
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(
                jsonPath("$.message")
                      .value("Conversation not found: " + id)
          );

    verify(conversationService).getConversation(id);
  }

  @Test
  void chatShouldReturnConversationAiResponse() throws Exception {

    UUID conversationId = UUID.randomUUID();

    when(conversationAIService.chat(
          conversationId,
          "What database did you say Brand Engine uses?"
    )).thenReturn("PostgreSQL.");

    mockMvc.perform(
                post(
                      "/api/v1/ai/conversations/{conversationId}/chat",
                      conversationId
                )
                      .contentType(MediaType.APPLICATION_JSON)
                      .content("""
                          {
                            "message": "What database did you say Brand Engine uses?"
                          }
                          """)
          )
          .andExpect(status().isOk())
          .andExpect(
                jsonPath("$.response")
                      .value("PostgreSQL.")
          );

    verify(conversationAIService).chat(
          conversationId,
          "What database did you say Brand Engine uses?"
    );
  }
}