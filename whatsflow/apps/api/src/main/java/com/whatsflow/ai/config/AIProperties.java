package com.whatsflow.ai.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "whatsflow.ai")
public class AIProperties {
    private String defaultProvider = "openai";
    private String fallbackProvider = "ollama";
    private String defaultModel = "gpt-4o-mini";
    private Provider openai = new Provider();
    private Provider gemini = new Provider();
    private Provider claude = new Provider();
    private Provider openrouter = new Provider();
    private Ollama ollama = new Ollama();
    private Azure azureOpenai = new Azure();

    @Getter @Setter public static class Provider {
        private String apiKey = "";
        private String baseUrl = "";
        private String model = "";
    }
    @Getter @Setter public static class Ollama {
        private String baseUrl = "http://localhost:11434";
        private String chatModel = "llama3.2";
    }
    @Getter @Setter public static class Azure {
        private String apiKey = "";
        private String baseUrl = "";
        private String deployment = "";
    }
}
