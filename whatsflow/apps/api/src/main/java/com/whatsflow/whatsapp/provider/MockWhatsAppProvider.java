package com.whatsflow.whatsapp.provider;


import com.whatsflow.whatsapp.spi.WhatsAppProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
@ConditionalOnProperty(name = "whatsapp.provider", havingValue = "mock", matchIfMissing = true)
public class MockWhatsAppProvider implements WhatsAppProvider {
    @Override public String id() { return "mock"; }
    @Override public Map<String, Object> sendText(String phoneNumberId, String accessToken, String to, String body) {
        return Map.of("messaging_product", "whatsapp", "messages", List.of(Map.of("id", "wamid.mock." + UUID.randomUUID())));
    }
    @Override public Map<String, Object> sendTemplate(String phoneNumberId, String accessToken, String to,
                                                      String templateName, String language, List<String> bodyParams) {
        return sendText(phoneNumberId, accessToken, to, templateName);
    }
    @Override public Map<String, Object> sendMedia(String phoneNumberId, String accessToken, String to, String type, String link, String caption) {
        return sendText(phoneNumberId, accessToken, to, type + ":" + link);
    }
}
