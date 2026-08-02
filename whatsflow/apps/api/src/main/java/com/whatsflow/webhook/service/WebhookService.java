package com.whatsflow.webhook.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsflow.config.WhatsAppProperties;
import com.whatsflow.message.domain.Message;
import com.whatsflow.message.repository.MessageRepository;
import com.whatsflow.webhook.domain.WebhookEvent;
import com.whatsflow.webhook.repository.WebhookEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebhookService {
    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private final WhatsAppProperties props;
    private final WebhookEventRepository events;
    private final MessageRepository messages;
    private final ObjectMapper mapper;

    public WebhookService(WhatsAppProperties props, WebhookEventRepository events,
                          MessageRepository messages, ObjectMapper mapper) {
        this.props = props; this.events = events; this.messages = messages; this.mapper = mapper;
    }

    public boolean verify(String mode, String token, String challenge) {
        return "subscribe".equals(mode) && props.getVerifyToken().equals(token) && challenge != null;
    }

    @Transactional
    public void handle(String rawBody) {
        WebhookEvent event = new WebhookEvent();
        event.setPayloadJson(rawBody);
        event.setEventType("whatsapp");
        event.setProcessStatus("RECEIVED");
        events.save(event);
        try {
            JsonNode root = mapper.readTree(rawBody);
            JsonNode statuses = root.path("entry").path(0).path("changes").path(0).path("value").path("statuses");
            if (statuses.isArray()) {
                for (JsonNode st : statuses) {
                    String id = st.path("id").asText(null);
                    String status = st.path("status").asText(null);
                    if (id != null) {
                        messages.findByWaMessageId(id).ifPresent(m -> {
                            m.setDeliveryStatus(status);
                            if (st.has("errors")) m.setMetaErrorsJson(st.get("errors").toString());
                            messages.save(m);
                        });
                    }
                }
            }
            event.setProcessStatus("PROCESSED");
        } catch (Exception ex) {
            log.warn("Webhook process error: {}", ex.getMessage());
            event.setProcessStatus("FAILED");
        }
        events.save(event);
    }
}
