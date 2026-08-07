package com.djimbinov.platform.ai.model;

import java.util.List;

public record ChatResponseBody(

      List<Choice> choices

) {

  public record Choice(

        ChatMessage message

  ) {
  }
}