package com.whatsflow.whatsapp.provider;


import com.whatsflow.config.WhatsAppProperties;
import com.whatsflow.whatsapp.spi.WhatsAppProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.*;

@Component
@ConditionalOnProperty(name = "whatsapp.provider", havingValue = "meta")
public class MetaCloudProvider implements WhatsAppProvider {
    private final WhatsAppProperties props;
    private final RestClient.Builder builder;

    public MetaCloudProvider(WhatsAppProperties props, RestClient.Builder builder) {
        this.props = props; this.builder = builder;
    }

    @Override public String id() { return "meta"; }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> sendText(String phoneNumberId, String accessToken, String to, String body) {
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "text",
                "text", Map.of("body", body)
        );
        return post(phoneNumberId, accessToken, payload);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> sendTemplate(String phoneNumberId, String accessToken, String to,
                                            String templateName, String language, List<String> bodyParams) {
        List<Map<String, Object>> params = bodyParams == null ? List.of() :
                bodyParams.stream().map(p -> Map.<String, Object>of("type", "text", "text", p)).toList();
        Map<String, Object> template = new HashMap<>();
        template.put("name", templateName);
        template.put("language", Map.of("code", language));
        if (!params.isEmpty()) {
            template.put("components", List.of(Map.of("type", "body", "parameters", params)));
        }
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "template",
                "template", template
        );
        return post(phoneNumberId, accessToken, payload);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> sendMedia(String phoneNumberId, String accessToken, String to, String type, String link, String caption) {
        Map<String, Object> media = new HashMap<>();
        media.put("link", link);
        if (caption != null) media.put("caption", caption);
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", type,
                type, media
        );
        return post(phoneNumberId, accessToken, payload);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String phoneNumberId, String accessToken, Map<String, Object> payload) {
        String url = "https://graph.facebook.com/" + props.getApiVersion() + "/" + phoneNumberId + "/messages";
        return builder.build().post().uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .body(payload)
                .retrieve()
                .body(Map.class);
    }
}
