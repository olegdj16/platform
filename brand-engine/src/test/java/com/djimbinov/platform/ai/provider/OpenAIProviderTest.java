package com.djimbinov.platform.ai.provider;

import com.djimbinov.platform.ai.config.AIProperties;
import com.djimbinov.platform.ai.exception.AIServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAIProviderTest {

  @Mock
  private ExchangeFunction exchangeFunction;

  private OpenAIProvider openAIProvider;

  @BeforeEach
  void setUp() {

    WebClient webClient = WebClient.builder()
          .exchangeFunction(exchangeFunction)
          .build();

    AIProperties properties = new AIProperties();

    AIProperties.OpenAI openAI = new AIProperties.OpenAI();
    openAI.setApiKey("test-api-key");
    openAI.setModel("gpt-5.6");

    properties.setProvider("openai");
    properties.setOpenai(openAI);

    openAIProvider = new OpenAIProvider(
          webClient,
          properties
    );
  }

  @Test
  void chatShouldReturnOutputText() {

    String responseBody = """
          {
            "output": [
              {
                "type": "message",
                "content": [
                  {
                    "type": "output_text",
                    "text": "Hello from OpenAI!"
                  }
                ]
              }
            ]
          }
          """;

    ClientResponse clientResponse = ClientResponse
          .create(HttpStatus.OK)
          .header("Content-Type", "application/json")
          .body(responseBody)
          .build();

    when(exchangeFunction.exchange(any()))
          .thenReturn(Mono.just(clientResponse));

    String result = openAIProvider.chat("Hello");

    assertEquals("Hello from OpenAI!", result);

    verify(exchangeFunction).exchange(any());
  }

  @Test
  void chatShouldThrowAIServiceExceptionWhenOpenAIReturnsError() {

    ClientResponse clientResponse = ClientResponse
          .create(HttpStatus.TOO_MANY_REQUESTS)
          .header("Content-Type", "application/json")
          .body("""
                {
                  "error": {
                    "message": "Rate limit exceeded"
                  }
                }
                """)
          .build();

    when(exchangeFunction.exchange(any()))
          .thenReturn(Mono.just(clientResponse));

    AIServiceException exception = assertThrows(
          AIServiceException.class,
          () -> openAIProvider.chat("Hello")
    );

    assertEquals(
          "OpenAI request failed with status 429 TOO_MANY_REQUESTS",
          exception.getMessage()
    );

    verify(exchangeFunction).exchange(any());
  }

  @Test
  void chatShouldThrowAIServiceExceptionWhenResponseHasNoOutputText() {

    String responseBody = """
          {
            "output": []
          }
          """;

    ClientResponse clientResponse = ClientResponse
          .create(HttpStatus.OK)
          .header("Content-Type", "application/json")
          .body(responseBody)
          .build();

    when(exchangeFunction.exchange(any()))
          .thenReturn(Mono.just(clientResponse));

    AIServiceException exception = assertThrows(
          AIServiceException.class,
          () -> openAIProvider.chat("Hello")
    );

    assertEquals(
          "OpenAI response did not contain output text",
          exception.getMessage()
    );

    verify(exchangeFunction).exchange(any());
  }
}