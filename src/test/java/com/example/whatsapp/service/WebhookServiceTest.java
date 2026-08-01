package com.example.whatsapp.service;

import com.example.whatsapp.dto.WhatsAppWebhookChange;
import com.example.whatsapp.dto.WhatsAppWebhookEntry;
import com.example.whatsapp.dto.WhatsAppWebhookMessage;
import com.example.whatsapp.dto.WhatsAppWebhookPayload;
import com.example.whatsapp.dto.WhatsAppWebhookText;
import com.example.whatsapp.dto.WhatsAppWebhookValue;
import com.example.whatsapp.exception.WhatsAppApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private ConversationService conversationService;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Mock
    private WhatsAppService whatsAppService;

    @InjectMocks
    private WebhookService webhookService;

    @Test
    void processWebhook_savesInboundAndSendsAutoReply() throws Exception {
        WhatsAppWebhookPayload payload = buildPayload("Hello");

        when(objectMapper.writeValueAsString(payload)).thenReturn("{}");

        webhookService.processWebhook(payload);

        verify(conversationService).saveInbound(eq("917506426501"), eq("Hello"), any());
        verify(whatsAppService).sendAutoReply("917506426501");
    }

    @Test
    void processWebhook_continuesWhenAutoReplyFails() throws Exception {
        WhatsAppWebhookPayload payload = buildPayload("Hello");

        when(objectMapper.writeValueAsString(payload)).thenReturn("{}");
        doThrow(new WhatsAppApiException("API down", 502))
                .when(whatsAppService).sendAutoReply("917506426501");

        webhookService.processWebhook(payload);

        verify(conversationService).saveInbound(eq("917506426501"), eq("Hello"), any());
        verify(whatsAppService).sendAutoReply("917506426501");
    }

    @Test
    void processWebhook_ignoresUnexpectedObject() throws Exception {
        WhatsAppWebhookPayload payload = buildPayload("Hello");
        payload.setObject("unknown_object");

        when(objectMapper.writeValueAsString(payload)).thenReturn("{}");

        webhookService.processWebhook(payload);

        verify(conversationService, never()).saveInbound(any(), any(), any());
        verify(whatsAppService, never()).sendAutoReply(any());
    }

    private WhatsAppWebhookPayload buildPayload(String body) {
        WhatsAppWebhookMessage message = new WhatsAppWebhookMessage();
        message.setFrom("917506426501");
        message.setId("wamid.test");
        message.setTimestamp("1710000000");
        message.setType("text");
        WhatsAppWebhookText text = new WhatsAppWebhookText();
        text.setBody(body);
        message.setText(text);

        WhatsAppWebhookValue value = new WhatsAppWebhookValue();
        value.setMessagingProduct("whatsapp");
        value.setMessages(List.of(message));

        WhatsAppWebhookChange change = new WhatsAppWebhookChange();
        change.setField("messages");
        change.setValue(value);

        WhatsAppWebhookEntry entry = new WhatsAppWebhookEntry();
        entry.setId("entry-id");
        entry.setChanges(List.of(change));

        WhatsAppWebhookPayload payload = new WhatsAppWebhookPayload();
        payload.setObject("whatsapp_business_account");
        payload.setEntry(List.of(entry));
        return payload;
    }
}
