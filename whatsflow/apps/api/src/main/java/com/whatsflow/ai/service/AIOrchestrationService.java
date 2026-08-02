package com.whatsflow.ai.service;


import com.whatsflow.ai.spi.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AIOrchestrationService {
    private final AIProviderRouter router;
    public AIOrchestrationService(AIProviderRouter router) { this.router = router; }

    public AIChatResponse run(AICapability capability, String providerId, String system, String user) {
        AIProvider provider = router.resolveFor(capability, providerId);
        return provider.chat(new AIChatRequest(null,
                List.of(new AIChatMessage("system", system), new AIChatMessage("user", user)), 0.3, 1024));
    }

    public AIChatResponse summarize(String transcript) {
        return run(AICapability.SUMMARIZE, null, "Summarize briefly.", transcript);
    }
    public AIChatResponse suggestReply(String transcript) {
        return run(AICapability.SUGGEST, null, "Suggest a short WhatsApp reply.", transcript);
    }
    public AIChatResponse intent(String text) { return run(AICapability.INTENT, null, "Return intent label only.", text); }
    public AIChatResponse sentiment(String text) { return run(AICapability.SENTIMENT, null, "Return positive|neutral|negative.", text); }
    public AIChatResponse translate(String text, String lang) { return run(AICapability.TRANSLATE, null, "Translate to " + lang, text); }
    public AIChatResponse campaignCopy(String brief) { return run(AICapability.COPYWRITE, null, "Write WA marketing copy.", brief); }
    public AIChatResponse qualifyLead(String profile) { return run(AICapability.LEAD_SCORE, null, "Score 0-100 with reason.", profile); }
}
