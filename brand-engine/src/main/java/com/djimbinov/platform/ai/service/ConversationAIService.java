package com.djimbinov.platform.ai.service;

import com.djimbinov.platform.ai.model.ChatMessageEntity;
import com.djimbinov.platform.ai.model.MessageRole;
import com.djimbinov.platform.ai.provider.AIProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ConversationAIService {

  private final ChatMessageService chatMessageService;
  private final AIProvider aiProvider;

  public ConversationAIService(
        ChatMessageService chatMessageService,
        AIProvider aiProvider
  ) {
    this.chatMessageService = chatMessageService;
    this.aiProvider = aiProvider;
  }

  public String chat(
        UUID conversationId,
        String message
  ) {

    chatMessageService.saveUserMessage(
          conversationId,
          message
    );

    List<ChatMessageEntity> history =
          chatMessageService.getConversationMessages(
                conversationId
          );

    String prompt = buildPrompt(history);

    String response =
          aiProvider.chat(prompt);

    chatMessageService.saveAssistantMessage(
          conversationId,
          response
    );

    return response;
  }

  private String buildPrompt(
        List<ChatMessageEntity> history
  ) {

    StringBuilder prompt = new StringBuilder();

    prompt.append("""
          Continue the conversation using the history below.
          Use previous messages when they are relevant to the user's latest message.

          CONVERSATION HISTORY:
          
          """);

    for (ChatMessageEntity message : history) {

      if (message.getRole() == MessageRole.USER) {
        prompt.append("USER: ");
      } else {
        prompt.append("ASSISTANT: ");
      }

      prompt.append(message.getContent())
            .append("\n");
    }

    prompt.append("""
          
          Respond to the latest USER message.
          """);

    return prompt.toString();
  }
}