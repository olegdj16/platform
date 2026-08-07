package com.djimbinov.platform.ai.provider;

import com.djimbinov.platform.ai.config.AIProperties;
import com.djimbinov.platform.ai.exception.AIServiceException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Component
@Primary
public class OpenAIProvider implements AIProvider {

  private final WebClient webClient;
  private final AIProperties aiProperties;

  public OpenAIProvider(
        WebClient webClient,
        AIProperties aiProperties
  ) {
    this.webClient = webClient;
    this.aiProperties = aiProperties;
  }

  @Override
  public String chat(String prompt) {

    Map<String, Object> requestBody = Map.of(
          "model", aiProperties.getOpenai().getModel(),
          "input", prompt
    );

    try {
      OpenAIResponse response = webClient
            .post()
            .uri("https://api.openai.com/v1/responses")
            .header(
                  "Authorization",
                  "Bearer " + aiProperties.getOpenai().getApiKey()
            )
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(OpenAIResponse.class)
            .block();

      if (response == null || response.output() == null) {
        throw new AIServiceException(
              "OpenAI returned an empty response"
        );
      }

      return response.output().stream()
            .filter(item -> "message".equals(item.type()))
            .flatMap(item -> item.content().stream())
            .filter(content -> "output_text".equals(content.type()))
            .map(Content::text)
            .findFirst()
            .orElseThrow(() ->
                  new AIServiceException(
                        "OpenAI response did not contain output text"
                  )
            );

    } catch (WebClientResponseException exception) {
      throw new AIServiceException(
            "OpenAI request failed with status "
                  + exception.getStatusCode(),
            exception
      );
    }
  }

  private record OpenAIResponse(
        List<OutputItem> output
  ) {
  }

  private record OutputItem(
        String type,
        List<Content> content
  ) {
  }

  private record Content(
        String type,
        String text
  ) {
  }
}