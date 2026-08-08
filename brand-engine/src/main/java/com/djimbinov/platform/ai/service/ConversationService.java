package com.djimbinov.platform.ai.service;

import com.djimbinov.platform.ai.dto.ConversationRequest;
import com.djimbinov.platform.ai.dto.ConversationResponse;
import com.djimbinov.platform.ai.exception.ConversationNotFoundException;
import com.djimbinov.platform.ai.mapper.ConversationMapper;
import com.djimbinov.platform.ai.model.Conversation;
import com.djimbinov.platform.ai.repository.ConversationRepository;
import com.djimbinov.platform.user.model.User;
import com.djimbinov.platform.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

  private final ConversationRepository conversationRepository;
  private final ConversationMapper conversationMapper;
  private final UserRepository userRepository;

  public ConversationService(
        ConversationRepository conversationRepository,
        ConversationMapper conversationMapper,
        UserRepository userRepository
  ) {
    this.conversationRepository = conversationRepository;
    this.conversationMapper = conversationMapper;
    this.userRepository = userRepository;
  }

  @Transactional
  public ConversationResponse createConversation(
        ConversationRequest request
  ) {

    User currentUser = getCurrentUser();

    Conversation conversation =
          conversationMapper.toEntity(
                currentUser.getId(),
                request.title()
          );

    Conversation savedConversation =
          conversationRepository.save(conversation);

    return conversationMapper.toResponse(savedConversation);
  }

  @Transactional(readOnly = true)
  public ConversationResponse getConversation(
        UUID id
  ) {

    Conversation conversation =
          conversationRepository.findById(id)
                .orElseThrow(() ->
                      new ConversationNotFoundException(id)
                );

    return conversationMapper.toResponse(conversation);
  }

  @Transactional(readOnly = true)
  public List<ConversationResponse> getConversations() {

    return conversationRepository.findAll()
          .stream()
          .map(conversationMapper::toResponse)
          .toList();
  }

  @Transactional
  public void deleteConversation(
        UUID id
  ) {

    Conversation conversation =
          conversationRepository.findById(id)
                .orElseThrow(() ->
                      new ConversationNotFoundException(id)
                );

    conversationRepository.delete(conversation);
  }

  private User getCurrentUser() {

    Authentication authentication =
          SecurityContextHolder
                .getContext()
                .getAuthentication();

    String email = authentication.getName();

    return userRepository
          .findByEmailIgnoreCase(email)
          .orElseThrow(() ->
                new IllegalStateException(
                      "Authenticated user not found: " + email
                )
          );
  }
}