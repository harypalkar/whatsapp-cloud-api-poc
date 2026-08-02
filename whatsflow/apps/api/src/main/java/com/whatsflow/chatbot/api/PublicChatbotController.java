package com.whatsflow.chatbot.api;

import com.whatsflow.ai.provider.MockAIProvider;
import com.whatsflow.ai.service.AIOrchestrationService;
import com.whatsflow.ai.spi.AICapability;
import com.whatsflow.ai.spi.AIChatResponse;
import com.whatsflow.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/public/chatbot")
@Tag(name = "Public Chatbot")
public class PublicChatbotController {

    private final AIOrchestrationService ai;

    public PublicChatbotController(AIOrchestrationService ai) {
        this.ai = ai;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> chat(@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "").trim();
        if (message.isBlank()) {
            message = "hello";
        }
        String reply;
        String source = "assistant";
        try {
            AIChatResponse res = ai.run(
                    AICapability.CHAT,
                    "mock",
                    "You are WhatsFlow assistant. Answer briefly about WhatsFlow product only.",
                    message);
            reply = res.content() == null || res.content().isBlank()
                    ? MockAIProvider.answer(message)
                    : res.content();
        } catch (Exception ex) {
            reply = MockAIProvider.answer(message);
            source = "faq";
        }
        return ApiResponse.ok(Map.of(
                "reply", reply,
                "source", source
        ));
    }
}
