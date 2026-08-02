package com.whatsflow.ai.spi;


public record AIChatResponse(String model, String content, Integer promptTokens, Integer completionTokens, long latencyMs) {}
