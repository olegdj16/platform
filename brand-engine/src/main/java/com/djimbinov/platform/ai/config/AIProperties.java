package com.djimbinov.platform.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
public class AIProperties {

  private String provider;
  private OpenAI openai = new OpenAI();

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public OpenAI getOpenai() {
    return openai;
  }

  public void setOpenai(OpenAI openai) {
    this.openai = openai;
  }

  public static class OpenAI {

    private String apiKey;
    private String model;

    public String getApiKey() {
      return apiKey;
    }

    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }

    public String getModel() {
      return model;
    }

    public void setModel(String model) {
      this.model = model;
    }
  }
}