package com.example.whatsapp.controller;

import com.example.whatsapp.service.WhatsAppCredentialResolver;
import com.example.whatsapp.service.WhatsAppCredentialResolver.ResolvedCredentials;
import com.example.whatsapp.service.WhatsAppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MessageV1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WhatsAppService whatsAppService;

    @MockBean
    private WhatsAppCredentialResolver credentialResolver;

    @BeforeEach
    void credentials() {
        when(credentialResolver.resolve()).thenReturn(ResolvedCredentials.builder()
                .companyId(1L)
                .accessToken("test-token")
                .phoneNumberId("1226308087231072")
                .apiVersion("v23.0")
                .templateName("altitude_welcome_promo")
                .templateLanguage("en")
                .displayPhoneNumber("919512618333")
                .build());
    }

    @Test
    void send_returnsWrappedMetaResponse() throws Exception {
        when(whatsAppService.sendV1(any())).thenReturn(Map.of(
                "messaging_product", "whatsapp",
                "messages", java.util.List.of(Map.of("id", "wamid.test"))));

        mockMvc.perform(post("/api/v1/messages/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerNumber":"917506426501",
                                  "customerName":"Harish",
                                  "promoCode":"WELCOME100",
                                  "message":"Hello Harish, welcome to Altitude Labs.",
                                  "type":"text"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.receiver").value("917506426501"))
                .andExpect(jsonPath("$.companyId").value(1));
    }

    @Test
    void sendTemplate_returnsWrappedMetaResponse() throws Exception {
        when(whatsAppService.sendPromoTemplate(any())).thenReturn(Map.of(
                "messaging_product", "whatsapp",
                "messages", java.util.List.of(Map.of("id", "wamid.template"))));

        mockMvc.perform(post("/api/v1/messages/send-template")
                        .header("X-Company-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mobile":"917506426501",
                                  "customerName":"Harish",
                                  "promoCode":"WELCOME100"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.templateName").value("altitude_welcome_promo"))
                .andExpect(jsonPath("$.receiver").value("917506426501"));
    }
}
