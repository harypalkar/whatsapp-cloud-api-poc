package com.example.whatsapp.service;

import com.example.whatsapp.dto.IncomingMessageDto;
import com.example.whatsapp.dto.WhatsAppWebhookMessage;
import com.example.whatsapp.dto.WhatsAppWebhookPayload;
import com.example.whatsapp.dto.WhatsAppWebhookStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private static final String WHATSAPP_OBJECT = "whatsapp_business_account";

    private final ConversationService conversationService;
    private final ObjectMapper objectMapper;
    private final WhatsAppService whatsAppService;

    public void processWebhook(WhatsAppWebhookPayload payload) {
        log.info("Webhook event received: object={}", payload.getObject());
        logPayload(payload);

        if (payload.getObject() != null && !WHATSAPP_OBJECT.equals(payload.getObject())) {
            log.warn("Webhook event ignored: unexpected object={}", payload.getObject());
            return;
        }

        if (payload.getEntry() == null) {
            log.warn("Webhook event ignored: no entries in payload");
            return;
        }

        for (var entry : payload.getEntry()) {
            if (entry.getChanges() == null) {
                continue;
            }

            for (var change : entry.getChanges()) {
                log.info("Webhook event: field={}", change.getField());
                if (change.getValue() == null) {
                    continue;
                }

                if (change.getValue().getStatuses() != null) {
                    for (WhatsAppWebhookStatus status : change.getValue().getStatuses()) {
                        handleStatus(status);
                    }
                }

                if (change.getValue().getMessages() != null) {
                    for (WhatsAppWebhookMessage message : change.getValue().getMessages()) {
                        extractMessage(message).ifPresent(this::handleIncomingMessage);
                    }
                }
            }
        }
    }

    private void handleStatus(WhatsAppWebhookStatus status) {
        if (status == null || status.getId() == null || status.getStatus() == null) {
            return;
        }
        log.info(
                "Delivery/read status: waMessageId={}, status={}, recipient={}",
                status.getId(),
                status.getStatus(),
                status.getRecipient_id());
        conversationService.updateDeliveryStatus(status.getId(), status.getStatus());
    }

    private void logPayload(WhatsAppWebhookPayload payload) {
        try {
            log.info("Webhook event payload: {}", objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException ex) {
            log.info("Webhook event payload: {}", payload);
        }
    }

    private Optional<IncomingMessageDto> extractMessage(WhatsAppWebhookMessage message) {
        if (message.getFrom() == null || message.getText() == null || message.getText().getBody() == null) {
            log.debug("Webhook event skipped: non-text or incomplete message id={}", message.getId());
            return Optional.empty();
        }

        LocalDateTime timestamp = parseTimestamp(message.getTimestamp());

        log.info(
                "Webhook event parsed: mobile={}, messageId={}, timestamp={}",
                message.getFrom(),
                message.getId(),
                timestamp);

        return Optional.of(IncomingMessageDto.builder()
                .mobile(message.getFrom())
                .message(message.getText().getBody())
                .timestamp(timestamp)
                .build());
    }

    private LocalDateTime parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return LocalDateTime.now(ZoneOffset.UTC);
        }

        try {
            return LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(Long.parseLong(timestamp)),
                    ZoneOffset.UTC);
        } catch (NumberFormatException ex) {
            log.warn("Invalid webhook timestamp '{}', using current time", timestamp);
            return LocalDateTime.now(ZoneOffset.UTC);
        }
    }

    private void handleIncomingMessage(IncomingMessageDto incoming) {
        conversationService.saveInbound(incoming.getMobile(), incoming.getMessage(), incoming.getTimestamp());

        try {
            whatsAppService.sendAutoReply(incoming.getMobile());
        } catch (Exception ex) {
            log.error("Auto reply failed for mobile={}: {}", incoming.getMobile(), ex.getMessage(), ex);
        }
    }
}
