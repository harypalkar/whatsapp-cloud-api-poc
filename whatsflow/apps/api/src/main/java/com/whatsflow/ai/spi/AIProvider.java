package com.whatsflow.ai.spi;


import java.util.List;
public interface AIProvider {
    String id();
    boolean supports(AICapability capability);
    AIChatResponse chat(AIChatRequest request);
    List<float[]> embed(AIEmbedRequest request);
}
