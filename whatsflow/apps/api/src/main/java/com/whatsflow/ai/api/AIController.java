package com.whatsflow.ai.api;


import com.whatsflow.ai.service.AIOrchestrationService;
import com.whatsflow.ai.spi.AICapability;
import com.whatsflow.ai.spi.AIChatResponse;
import com.whatsflow.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/v1/ai")
@Tag(name = "AI")
public class AIController {
    private final AIOrchestrationService ai;
    public AIController(AIOrchestrationService ai) { this.ai = ai; }

    @PostMapping("/chat")
    public ApiResponse<AIChatResponse> chat(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(ai.run(AICapability.CHAT, body.get("provider"),
                body.getOrDefault("systemPrompt", "You are WhatsFlow AI."),
                body.getOrDefault("userPrompt", "")));
    }
    @PostMapping("/summarize") public ApiResponse<AIChatResponse> summarize(@RequestBody Map<String,String> b) { return ApiResponse.ok(ai.summarize(b.get("text"))); }
    @PostMapping("/suggest-reply") public ApiResponse<AIChatResponse> suggest(@RequestBody Map<String,String> b) { return ApiResponse.ok(ai.suggestReply(b.get("text"))); }
    @PostMapping("/intent") public ApiResponse<AIChatResponse> intent(@RequestBody Map<String,String> b) { return ApiResponse.ok(ai.intent(b.get("text"))); }
    @PostMapping("/sentiment") public ApiResponse<AIChatResponse> sentiment(@RequestBody Map<String,String> b) { return ApiResponse.ok(ai.sentiment(b.get("text"))); }
    @PostMapping("/translate") public ApiResponse<AIChatResponse> translate(@RequestBody Map<String,String> b) { return ApiResponse.ok(ai.translate(b.get("text"), b.getOrDefault("targetLang","hi"))); }
    @PostMapping("/campaign-writer") public ApiResponse<AIChatResponse> campaign(@RequestBody Map<String,String> b) { return ApiResponse.ok(ai.campaignCopy(b.get("brief"))); }
    @PostMapping("/lead-qualify") public ApiResponse<AIChatResponse> lead(@RequestBody Map<String,String> b) { return ApiResponse.ok(ai.qualifyLead(b.get("profile"))); }
}
