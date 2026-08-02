package com.whatsflow.ai.provider;

import com.whatsflow.ai.spi.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class MockAIProvider implements AIProvider {

    @Override
    public String id() {
        return "mock";
    }

    @Override
    public boolean supports(AICapability capability) {
        return true;
    }

    @Override
    public AIChatResponse chat(AIChatRequest request) {
        String user = "";
        if (request.messages() != null) {
            for (int i = request.messages().size() - 1; i >= 0; i--) {
                AIChatMessage m = request.messages().get(i);
                if ("user".equalsIgnoreCase(m.role())) {
                    user = m.content() == null ? "" : m.content();
                    break;
                }
            }
        }
        return new AIChatResponse("mock", answer(user), 0, 0, 12L);
    }

    @Override
    public List<float[]> embed(AIEmbedRequest request) {
        return Collections.emptyList();
    }

    public static String answer(String user) {
        String lower = user == null ? "" : user.toLowerCase();
        if (lower.contains("price") || lower.contains("plan") || lower.contains("cost") || lower.contains("pricing")) {
            return "WhatsFlow plans: Starter ₹999/mo, Growth ₹4,999/mo, and custom Enterprise. Start free from Sign up and choose a plan in onboarding.";
        }
        if (lower.contains("whatsapp") || lower.contains("meta") || lower.contains("waba") || lower.contains("connect")) {
            return "Connect WhatsApp via Meta Embedded Signup during onboarding. We store phone number ID, WABA, and tokens securely per company.";
        }
        if (lower.contains("inbox") || lower.contains("live chat") || lower.contains("agent")) {
            return "The shared Live Chat inbox lets agents reply together. Open it from the console after onboarding.";
        }
        if (lower.contains("campaign") || lower.contains("bulk") || lower.contains("template")) {
            return "Create template campaigns from Campaigns after your WhatsApp number is connected and templates are approved in Meta.";
        }
        if (lower.contains("ai") || lower.contains("rag") || lower.contains("suggest")) {
            return "AI Studio can suggest replies and use your knowledge base. Try it from the console under AI Studio.";
        }
        if (lower.contains("onboard") || lower.contains("start") || lower.contains("register") || lower.contains("signup") || lower.contains("sign up")) {
            return "Click Start free, create your company, then finish the 5-step onboarding: business details, plan, Meta signup, WhatsApp connect, success.";
        }
        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey") || lower.isBlank()) {
            return "Hi — I am the WhatsFlow assistant. Ask about plans, WhatsApp setup, campaigns, inbox, or AI Studio.";
        }
        return "I can help with WhatsFlow plans, WhatsApp/Meta connect, campaigns, inbox, and AI Studio. What would you like to know?";
    }
}
