package com.whatsflow.whatsapp.spi;


import java.util.List;
import java.util.Map;

public interface WhatsAppProvider {
    String id();
    Map<String, Object> sendText(String phoneNumberId, String accessToken, String to, String body);
    Map<String, Object> sendTemplate(String phoneNumberId, String accessToken, String to,
                                     String templateName, String language, List<String> bodyParams);
    Map<String, Object> sendMedia(String phoneNumberId, String accessToken, String to, String type, String link, String caption);
}
