package com.djimbinov.platform.ai.service;

import com.djimbinov.platform.ai.dto.ConversationRequest;
import com.djimbinov.platform.ai.dto.ConversationResponse;
import com.djimbinov.platform.ai.exception.ConversationNotFoundException;
import com.djimbinov.platform.ai.mapper.ConversationMapper;
import com.djimbinov.platform.ai.model.Conversation;
import com.djimbinov.platform.ai.repository.ConversationRepository;
import com.djimbinov.platform.user.model.User;
import com.djimbinov.platform.user.model.UserRole;
import com.djimbinov.platform.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

  @Mock
  private ConversationRepository conversationRepository;

  @Mock
  private ConversationMapper conversationMapper;

  @Mock
  private UserRepository userRepository;

  private ConversationService conversationService;

  @BeforeEach
  void setUp() {
    conversationService = new ConversationService(
          conversationRepository,
          conversationMapper,
          userRepository
    );
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldCreateConversation() {

    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    String email = "user@example.com";
    Instant createdAt = Instant.now();

    ConversationRequest request =
          new ConversationRequest(
                "Brand Engine Discussion"
          );

    User user =
          new User(
                userId,
                email,
                "password-hash",
                UserRole.USER,
                createdAt,
                createdAt
          );

    Conversation conversation =
          new Conversation(
                id,
                userId,
                request.title(),
                createdAt
          );

    Conversation savedConversation =
          new Conversation(
                id,
                userId,
                request.title(),
                createdAt
          );

    ConversationResponse expectedResponse =
          new ConversationResponse(
                id,
                request.title(),
                createdAt
          );

    UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
                email,
                null
          );

    SecurityContextHolder
          .getContext()
          .setAuthentication(authentication);

    when(userRepository.findByEmailIgnoreCase(email))
          .thenReturn(Optional.of(user));

    when(conversationMapper.toEntity(
          userId,
          request.title()
    )).thenReturn(conversation);

    when(conversationRepository.save(conversation))
          .thenReturn(savedConversation);

    when(conversationMapper.toResponse(savedConversation))
          .thenReturn(expectedResponse);

    ConversationResponse result =
          conversationService.createConversation(request);

    assertSame(expectedResponse, result);
    assertEquals(id, result.id());
    assertEquals(
          "Brand Engine Discussion",
          result.title()
    );

    verify(userRepository)
          .findByEmailIgnoreCase(email);

    verify(conversationMapper)
          .toEntity(
                userId,
                request.title()
          );

    verify(conversationRepository)
          .save(conversation);

    verify(conversationMapper)
          .toResponse(savedConversation);
  }

  @Test
  void shouldGetConversation() {

    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Instant createdAt = Instant.now();

    Conversation conversation =
          new Conversation(
                id,
                userId,
                "AI Discussion",
                createdAt
          );

    ConversationResponse expectedResponse =
          new ConversationResponse(
                id,
                "AI Discussion",
                createdAt
          );

    when(conversationRepository.findById(id))
          .thenReturn(Optional.of(conversation));

    when(conversationMapper.toResponse(conversation))
          .thenReturn(expectedResponse);

    ConversationResponse result =
          conversationService.getConversation(id);

    assertSame(expectedResponse, result);
    assertEquals(id, result.id());
    assertEquals(
          "AI Discussion",
          result.title()
    );

    verify(conversationRepository)
          .findById(id);

    verify(conversationMapper)
          .toResponse(conversation);

    verifyNoInteractions(userRepository);
  }

  @Test
  void shouldGetAllConversations() {

    UUID firstUserId = UUID.randomUUID();
    UUID secondUserId = UUID.randomUUID();

    Conversation firstConversation =
          new Conversation(
                UUID.randomUUID(),
                firstUserId,
                "First Conversation",
                Instant.now()
          );

    Conversation secondConversation =
          new Conversation(
                UUID.randomUUID(),
                secondUserId,
                "Second Conversation",
                Instant.now()
          );

    ConversationResponse firstResponse =
          new ConversationResponse(
                firstConversation.getId(),
                firstConversation.getTitle(),
                firstConversation.getCreatedAt()
          );

    ConversationResponse secondResponse =
          new ConversationResponse(
                secondConversation.getId(),
                secondConversation.getTitle(),
                secondConversation.getCreatedAt()
          );

    when(conversationRepository.findAll())
          .thenReturn(
                List.of(
                      firstConversation,
                      secondConversation
                )
          );

    when(conversationMapper.toResponse(firstConversation))
          .thenReturn(firstResponse);

    when(conversationMapper.toResponse(secondConversation))
          .thenReturn(secondResponse);

    List<ConversationResponse> results =
          conversationService.getConversations();

    assertEquals(2, results.size());

    assertEquals(
          "First Conversation",
          results.get(0).title()
    );

    assertEquals(
          "Second Conversation",
          results.get(1).title()
    );

    verify(conversationRepository)
          .findAll();

    verify(conversationMapper)
          .toResponse(firstConversation);

    verify(conversationMapper)
          .toResponse(secondConversation);

    verifyNoInteractions(userRepository);
  }

  @Test
  void shouldDeleteConversation() {

    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Conversation conversation =
          new Conversation(
                id,
                userId,
                "Conversation to Delete",
                Instant.now()
          );

    when(conversationRepository.findById(id))
          .thenReturn(Optional.of(conversation));

    conversationService.deleteConversation(id);

    verify(conversationRepository)
          .findById(id);

    verify(conversationRepository)
          .delete(conversation);

    verifyNoInteractions(conversationMapper);
    verifyNoInteractions(userRepository);
  }

  @Test
  void shouldThrowConversationNotFoundException() {

    UUID id = UUID.randomUUID();

    when(conversationRepository.findById(id))
          .thenReturn(Optional.empty());

    ConversationNotFoundException exception =
          assertThrows(
                ConversationNotFoundException.class,
                () -> conversationService.getConversation(id)
          );

    assertEquals(
          "Conversation not found: " + id,
          exception.getMessage()
    );

    verify(conversationRepository)
          .findById(id);

    verifyNoInteractions(conversationMapper);
    verifyNoInteractions(userRepository);
  }
}