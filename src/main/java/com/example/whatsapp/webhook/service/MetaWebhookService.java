package com.example.whatsapp.webhook.service;

import com.example.whatsapp.config.WhatsAppProperties;
import com.example.whatsapp.dto.WhatsAppWebhookPayload;
import com.example.whatsapp.service.WebhookService;
import com.example.whatsapp.webhook.entity.WebhookEvent;
import com.example.whatsapp.webhook.repository.WebhookEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaWebhookService {

    private final WhatsAppProperties whatsAppProperties;
    private final WebhookEventRepository webhookEventRepository;
    private final ObjectMapper objectMapper;
    private final WebhookService webhookService;

    public String verifySubscription(String mode, String verifyToken, String challenge) {
        log.info(
                "Webhook verification attempt: hub.mode={}, hub.verify_tokenPresent={}, hub.challengePresent={}",
                mode,
                verifyToken != null && !verifyToken.isBlank(),
                challenge != null && !challenge.isBlank());

        if (mode == null || mode.isBlank() || verifyToken == null || verifyToken.isBlank() || challenge == null || challenge.isBlank()) {
            log.warn("Webhook verification failed: missing required query parameters");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing hub.mode, hub.verify_token or hub.challenge");
        }

        boolean modeOk = "subscribe".equals(mode);
        boolean tokenOk = whatsAppProperties.getVerifyToken() != null
                && whatsAppProperties.getVerifyToken().equals(verifyToken);

        log.info("Webhook verification result: modeOk={}, tokenOk={}", modeOk, tokenOk);

        if (modeOk && tokenOk) {
            log.info("Webhook verification successful — returning hub.challenge");
            return challenge;
        }

        log.warn("Webhook verification rejected: mode or verify token mismatch");
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Verification failed");
    }

    @Transactional
    public String ingest(WhatsAppWebhookPayload payload) {
        String rawPayload;
        try {
            rawPayload = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            rawPayload = String.valueOf(payload);
        }

        log.info("Webhook POST payload stored length={}", rawPayload.length());
        log.debug("Webhook POST payload={}", rawPayload);

        WebhookEvent event = WebhookEvent.builder()
                .eventObject(payload != null ? payload.getObject() : null)
                .payload(rawPayload)
                .build();
        webhookEventRepository.save(event);

        if (payload != null) {
            webhookService.processWebhook(payload);
        }

        return "EVENT_RECEIVED";
    }
}
