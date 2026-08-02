package com.whatsflow.rag.service;


import com.whatsflow.ai.service.AIOrchestrationService;
import com.whatsflow.ai.spi.AICapability;
import com.whatsflow.ai.spi.AIChatResponse;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class RagQueryService {
    private final AIOrchestrationService ai;
    public RagQueryService(AIOrchestrationService ai) { this.ai = ai; }

    public Map<String, Object> ask(String question, List<String> contextChunks) {
        String context = contextChunks == null ? "" : String.join("\n---\n", contextChunks);
        AIChatResponse answer = ai.run(AICapability.RAG_CHAT, null,
                "Answer using only the provided context. If unknown, say you don't know.",
                "Context:\n" + context + "\n\nQuestion: " + question);
        return Map.of("answer", answer.content(), "citations", contextChunks == null ? List.of() : contextChunks);
    }
}
