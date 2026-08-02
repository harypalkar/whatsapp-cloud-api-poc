package com.whatsflow.ai.spi;


import java.util.List;
public record AIChatRequest(String model, List<AIChatMessage> messages, Double temperature, Integer maxTokens) {}
