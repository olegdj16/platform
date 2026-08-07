package com.djimbinov.platform.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(AIProperties.class)
public class WebClientConfig {

  @Bean
  public WebClient webClient() {
    return WebClient.builder().build();
  }
}