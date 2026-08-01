package com.example.whatsapp.service;

import com.example.whatsapp.dto.WhatsAppRequest;
import com.example.whatsapp.exception.WhatsAppApiException;
import com.example.whatsapp.service.WhatsAppCredentialResolver.ResolvedCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhatsAppServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private com.example.whatsapp.config.WhatsAppProperties whatsAppProperties;

    @Mock
    private WhatsAppCredentialResolver credentialResolver;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private WhatsAppService whatsAppService;

    @BeforeEach
    void setUpCredentials() {
        when(credentialResolver.resolve()).thenReturn(ResolvedCredentials.builder()
                .companyId(1L)
                .accessToken("test-token")
                .phoneNumberId("123456")
                .apiVersion("v23.0")
                .templateName("altitude_welcome_promo")
                .templateLanguage("en")
                .displayPhoneNumber("919512618333")
                .build());
    }

    @Test
    void sendMessage_savesOutboundOnSuccess() {
        WhatsAppRequest request = WhatsAppRequest.builder()
                .mobile("917506426501")
                .message("Hello User")
                .build();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("messages", "ok")));

        Map<String, Object> response = whatsAppService.sendMessage(request);

        assertThat(response).containsKey("messages");
        verify(conversationService).saveOutbound(
                eq("917506426501"), eq("Hello User"), isNull(), eq("text"), eq(1L));
    }

    @Test
    void sendMessage_doesNotSaveOutboundOnApiError() {
        WhatsAppRequest request = WhatsAppRequest.builder()
                .mobile("917506426501")
                .message("Hello User")
                .build();

        HttpStatusCodeException apiException = org.mockito.Mockito.mock(HttpStatusCodeException.class);
        when(apiException.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(apiException.getResponseBodyAsString()).thenReturn("{\"error\":\"invalid\"}");

        doThrow(apiException).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class));

        assertThatThrownBy(() -> whatsAppService.sendMessage(request))
                .isInstanceOf(WhatsAppApiException.class);

        verify(conversationService, never()).saveOutbound(any(), any(), any(), any(), anyLong());
    }
}
