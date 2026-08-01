package com.example.whatsapp.service;

import com.example.whatsapp.exception.BusinessException;
import com.example.whatsapp.exception.WhatsAppApiException;
import com.example.whatsapp.service.WhatsAppCredentialResolver.ResolvedCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Read-only Meta helpers used to discover WABA + approved templates for cold outreach.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppMetaAdminService {

    private static final String GRAPH = "https://graph.facebook.com";

    private final RestTemplate restTemplate;
    private final WhatsAppCredentialResolver credentialResolver;

    public Map<String, Object> phoneProfile() {
        ResolvedCredentials creds = requireCreds();
        URI url = UriComponentsBuilder
                .fromHttpUrl(GRAPH + "/" + creds.getApiVersion() + "/" + creds.getPhoneNumberId())
                .queryParam("fields", "id,display_phone_number,verified_name,quality_rating,code_verification_status")
                .build()
                .encode()
                .toUri();
        Map<String, Object> phone = get(url, creds.getAccessToken());
        phone.put("configuredBusinessAccountId", creds.getBusinessAccountId());
        return phone;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> listMessageTemplates() {
        ResolvedCredentials creds = requireCreds();
        String wabaId = resolveWabaId(creds);
        if (!StringUtils.hasText(wabaId)) {
            throw new BusinessException(
                    "Could not resolve WhatsApp Business Account ID. Set WHATSAPP_BUSINESS_ACCOUNT_ID in .env "
                            + "(Meta → WhatsApp → API Setup → WhatsApp Business Account ID).",
                    HttpStatus.BAD_REQUEST.value());
        }

        URI url = UriComponentsBuilder
                .fromHttpUrl(GRAPH + "/" + creds.getApiVersion() + "/" + wabaId + "/message_templates")
                .queryParam("limit", "100")
                .queryParam("fields", "name,status,language,category")
                .build()
                .encode()
                .toUri();
        Map<String, Object> templates = get(url, creds.getAccessToken());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("businessAccountId", wabaId);
        body.put("phoneNumberId", creds.getPhoneNumberId());
        body.put("configuredTemplateName", creds.getTemplateName());
        body.put("configuredTemplateLanguage", creds.getTemplateLanguage());
        body.put("templates", templates.get("data"));
        body.put("paging", templates.get("paging"));
        body.put("hint", "Cold promo requires status=APPROVED marketing template. Use that exact name + language in send-template.");
        return body;
    }

    private String resolveWabaId(ResolvedCredentials creds) {
        if (StringUtils.hasText(creds.getBusinessAccountId())) {
            return creds.getBusinessAccountId();
        }
        try {
            URI url = UriComponentsBuilder
                    .fromHttpUrl(GRAPH + "/" + creds.getApiVersion() + "/me/whatsapp_business_accounts")
                    .queryParam("fields", "id,name")
                    .queryParam("limit", "10")
                    .build()
                    .encode()
                    .toUri();
            Map<String, Object> body = get(url, creds.getAccessToken());
            Object data = body.get("data");
            if (data instanceof java.util.List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> first) {
                Object id = first.get("id");
                return id != null ? id.toString() : null;
            }
        } catch (Exception ex) {
            log.warn("Could not auto-resolve WABA from /me/whatsapp_business_accounts: {}", ex.getMessage());
        }
        return null;
    }

    private ResolvedCredentials requireCreds() {
        ResolvedCredentials creds = credentialResolver.resolve();
        if (!StringUtils.hasText(creds.getAccessToken()) || !StringUtils.hasText(creds.getPhoneNumberId())) {
            throw new BusinessException(
                    "WhatsApp is not configured. Set WHATSAPP_TOKEN and WHATSAPP_PHONE_NUMBER_ID.",
                    HttpStatus.SERVICE_UNAVAILABLE.value());
        }
        return creds;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> get(URI url, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            return response.getBody() != null ? response.getBody() : Map.of();
        } catch (HttpStatusCodeException ex) {
            log.error("Meta admin API error: status={}, body={}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new WhatsAppApiException(ex.getResponseBodyAsString(), ex.getStatusCode().value());
        }
    }
}
