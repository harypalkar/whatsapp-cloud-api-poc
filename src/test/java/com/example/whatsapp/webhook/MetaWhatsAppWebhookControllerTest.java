package com.example.whatsapp.webhook;

import com.example.whatsapp.webhook.repository.WebhookEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MetaWhatsAppWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebhookEventRepository webhookEventRepository;

    @Test
    void verify_success_returnsOnlyChallenge() throws Exception {
        mockMvc.perform(get("/webhook/whatsapp")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "AltitudeLabs@2026")
                        .param("hub.challenge", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("1234567890"));
    }

    @Test
    void verify_wrongToken_returns403() throws Exception {
        mockMvc.perform(get("/webhook/whatsapp")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "wrong-token")
                        .param("hub.challenge", "1234567890"))
                .andExpect(status().isForbidden());
    }

    @Test
    void verify_missingParameters_returns400() throws Exception {
        mockMvc.perform(get("/webhook/whatsapp")
                        .param("hub.mode", "subscribe"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verify_specPath_success() throws Exception {
        mockMvc.perform(get("/api/v1/webhooks/meta/whatsapp")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "AltitudeLabs@2026")
                        .param("hub.challenge", "abc-challenge"))
                .andExpect(status().isOk())
                .andExpect(content().string("abc-challenge"));
    }

    @Test
    void postWebhook_persistsAndReturnsEventReceived() throws Exception {
        long before = webhookEventRepository.count();

        String payload = """
                {
                  "object": "whatsapp_business_account",
                  "entry": []
                }
                """;

        mockMvc.perform(post("/webhook/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string("EVENT_RECEIVED"));

        assertThat(webhookEventRepository.count()).isEqualTo(before + 1);
    }

    @Test
    void health_returnsApplicationRunning() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Application Running"));
    }
}
