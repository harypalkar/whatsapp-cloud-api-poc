package com.example.whatsapp.service;

import com.example.whatsapp.config.WhatsAppProperties;
import com.example.whatsapp.dto.SendMessageV1Request;
import com.example.whatsapp.dto.SendTemplateRequest;
import com.example.whatsapp.dto.WhatsAppApiPayload;
import com.example.whatsapp.dto.WhatsAppRequest;
import com.example.whatsapp.dto.WhatsAppTemplateRequest;
import com.example.whatsapp.exception.BusinessException;
import com.example.whatsapp.exception.WhatsAppApiException;
import com.example.whatsapp.service.WhatsAppCredentialResolver.ResolvedCredentials;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppService {

    private static final String GRAPH_API_BASE_URL = "https://graph.facebook.com";

    private final RestTemplate restTemplate;
    private final WhatsAppProperties whatsAppProperties;
    private final WhatsAppCredentialResolver credentialResolver;
    private final ObjectMapper objectMapper;
    private final ConversationService conversationService;

    public Map<String, Object> sendMessage(WhatsAppRequest request) {
        WhatsAppApiPayload payload = WhatsAppApiPayload.builder()
                .messagingProduct("whatsapp")
                .recipientType("individual")
                .to(request.getMobile())
                .type("text")
                .text(WhatsAppApiPayload.TextContent.builder().body(request.getMessage()).build())
                .build();
        return exchangeAndSave(payload, request.getMobile(), request.getMessage(), "text");
    }

    public Map<String, Object> sendAutoReply(String mobile) {
        return sendMessage(WhatsAppRequest.builder()
                .mobile(mobile)
                .message(whatsAppProperties.getAutoReplyMessage())
                .build());
    }

    /**
     * Cold bulk/direct promo — approved marketing template (no customer Hi required).
     */
    public Map<String, Object> sendPromoTemplate(SendTemplateRequest request) {
        ResolvedCredentials creds = credentialResolver.resolve();
        int paramCount = whatsAppProperties.getTemplateBodyParamCount();
        List<String> bodyParams = null;
        if (paramCount >= 2) {
            bodyParams = List.of(
                    StringUtils.hasText(request.getCustomerName()) ? request.getCustomerName() : "Customer",
                    StringUtils.hasText(request.getPromoCode()) ? request.getPromoCode() : "WELCOME100");
        } else if (paramCount == 1) {
            bodyParams = List.of(
                    StringUtils.hasText(request.getCustomerName()) ? request.getCustomerName() : "Customer");
        }

        return sendTemplateMessage(WhatsAppTemplateRequest.builder()
                .mobile(normalizeMobile(request.getMobile()))
                .templateName(creds.getTemplateName())
                .languageCode(creds.getTemplateLanguage())
                .bodyParameters(bodyParams)
                .build());
    }

    public Map<String, Object> sendTemplateMessage(WhatsAppTemplateRequest request) {
        List<WhatsAppApiPayload.TemplateComponent> components = null;
        if (request.getBodyParameters() != null && !request.getBodyParameters().isEmpty()) {
            List<WhatsAppApiPayload.TemplateParameter> parameters = request.getBodyParameters().stream()
                    .map(text -> WhatsAppApiPayload.TemplateParameter.builder()
                            .type("text")
                            .text(text)
                            .build())
                    .toList();
            components = List.of(WhatsAppApiPayload.TemplateComponent.builder()
                    .type("body")
                    .parameters(parameters)
                    .build());
        }

        WhatsAppApiPayload payload = WhatsAppApiPayload.builder()
                .messagingProduct("whatsapp")
                .to(request.getMobile())
                .type("template")
                .template(WhatsAppApiPayload.TemplateContent.builder()
                        .name(request.getTemplateName())
                        .language(WhatsAppApiPayload.TemplateLanguage.builder()
                                .code(request.getLanguageCode() != null ? request.getLanguageCode() : "en")
                                .build())
                        .components(components)
                        .build())
                .build();
        return exchangeAndSave(payload, request.getMobile(), "Template: " + request.getTemplateName(), "template");
    }

    /**
     * Unified send — text / template / media / location / interactive buttons.
     */
    public Map<String, Object> sendV1(SendMessageV1Request request) {
        ensureConfigured(credentialResolver.resolve());
        String type = (request.getType() == null ? "text" : request.getType()).toLowerCase(Locale.ROOT);
        String body = buildWelcomeBody(request);
        String mobile = normalizeMobile(request.getCustomerNumber());
        ResolvedCredentials creds = credentialResolver.resolve();

        log.info(
                "V1 send: companyId={}, fromDisplay={}, to={}, type={}, promo={}",
                creds.getCompanyId(),
                creds.getDisplayPhoneNumber(),
                mobile,
                type,
                request.getPromoCode());

        return switch (type) {
            case "template" -> sendTemplateMessage(WhatsAppTemplateRequest.builder()
                    .mobile(mobile)
                    .templateName(StringUtils.hasText(request.getTemplateName())
                            ? request.getTemplateName()
                            : creds.getTemplateName())
                    .languageCode(StringUtils.hasText(request.getLanguageCode())
                            ? request.getLanguageCode()
                            : creds.getTemplateLanguage())
                    .bodyParameters(List.of(
                            StringUtils.hasText(request.getCustomerName()) ? request.getCustomerName() : "Customer",
                            StringUtils.hasText(request.getPromoCode()) ? request.getPromoCode() : "WELCOME100"))
                    .build());
            case "image" -> sendMedia(mobile, "image", request.getMediaUrl(),
                    request.getCaption() != null ? request.getCaption() : body, null);
            case "document", "pdf" -> sendMedia(mobile, "document", request.getMediaUrl(),
                    request.getCaption() != null ? request.getCaption() : body,
                    request.getFilename() != null ? request.getFilename() : "document.pdf");
            case "video" -> sendMedia(mobile, "video", request.getMediaUrl(),
                    request.getCaption() != null ? request.getCaption() : body, null);
            case "audio" -> sendMedia(mobile, "audio", request.getMediaUrl(), null, null);
            case "location" -> sendLocation(mobile, request);
            case "interactive", "buttons" -> sendInteractiveButtons(mobile, body);
            default -> sendMessage(WhatsAppRequest.builder()
                    .mobile(mobile)
                    .message(body)
                    .build());
        };
    }

    public Map<String, Object> sendMedia(
            String mobile, String mediaType, String mediaUrl, String caption, String filename) {
        if (!StringUtils.hasText(mediaUrl)) {
            throw new BusinessException("mediaUrl is required for type=" + mediaType, HttpStatus.BAD_REQUEST.value());
        }

        WhatsAppApiPayload.MediaContent media = WhatsAppApiPayload.MediaContent.builder()
                .link(mediaUrl)
                .caption(caption)
                .filename(filename)
                .build();

        WhatsAppApiPayload.WhatsAppApiPayloadBuilder builder = WhatsAppApiPayload.builder()
                .messagingProduct("whatsapp")
                .to(mobile)
                .type(mediaType);

        switch (mediaType) {
            case "image" -> builder.image(media);
            case "document" -> builder.document(media);
            case "video" -> builder.video(media);
            case "audio" -> builder.audio(media);
            default -> throw new BusinessException("Unsupported media type: " + mediaType, 400);
        }

        return exchangeAndSave(builder.build(), mobile, mediaType + ": " + mediaUrl, mediaType);
    }

    public Map<String, Object> sendLocation(String mobile, SendMessageV1Request request) {
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new BusinessException("latitude and longitude are required for type=location", 400);
        }
        WhatsAppApiPayload payload = WhatsAppApiPayload.builder()
                .messagingProduct("whatsapp")
                .to(mobile)
                .type("location")
                .location(WhatsAppApiPayload.LocationContent.builder()
                        .latitude(request.getLatitude())
                        .longitude(request.getLongitude())
                        .name(request.getLocationName())
                        .address(request.getLocationAddress())
                        .build())
                .build();
        return exchangeAndSave(payload, mobile, "location", "location");
    }

    public Map<String, Object> sendShopCta(String mobile, String bodyText, String buttonText, String url) {
        WhatsAppApiPayload payload = WhatsAppApiPayload.builder()
                .messagingProduct("whatsapp")
                .to(mobile)
                .type("interactive")
                .interactive(WhatsAppApiPayload.InteractiveContent.builder()
                        .type("cta_url")
                        .body(WhatsAppApiPayload.InteractiveBody.builder().text(bodyText).build())
                        .action(WhatsAppApiPayload.InteractiveAction.builder()
                                .name("cta_url")
                                .parameters(WhatsAppApiPayload.CtaUrlParameters.builder()
                                        .displayText(buttonText)
                                        .url(url)
                                        .build())
                                .build())
                        .build())
                .build();
        return exchangeAndSave(payload, mobile, bodyText, "interactive");
    }

    public Map<String, Object> sendInteractiveButtons(String mobile, String bodyText) {
        WhatsAppApiPayload payload = WhatsAppApiPayload.builder()
                .messagingProduct("whatsapp")
                .to(mobile)
                .type("interactive")
                .interactive(WhatsAppApiPayload.InteractiveContent.builder()
                        .type("button")
                        .body(WhatsAppApiPayload.InteractiveBody.builder().text(bodyText).build())
                        .action(WhatsAppApiPayload.InteractiveAction.builder()
                                .buttons(List.of(
                                        WhatsAppApiPayload.InteractiveButton.builder()
                                                .type("reply")
                                                .reply(WhatsAppApiPayload.InteractiveReply.builder()
                                                        .id("interested")
                                                        .title("Interested")
                                                        .build())
                                                .build(),
                                        WhatsAppApiPayload.InteractiveButton.builder()
                                                .type("reply")
                                                .reply(WhatsAppApiPayload.InteractiveReply.builder()
                                                        .id("stop")
                                                        .title("Stop")
                                                        .build())
                                                .build()))
                                .build())
                        .build())
                .build();
        return exchangeAndSave(payload, mobile, bodyText, "interactive");
    }

    public String buildWelcomeBody(SendMessageV1Request request) {
        if (StringUtils.hasText(request.getMessage())
                && !"text".equalsIgnoreCase(request.getType())
                && !"interactive".equalsIgnoreCase(request.getType())
                && !"buttons".equalsIgnoreCase(request.getType())) {
            return request.getMessage();
        }

        String name = StringUtils.hasText(request.getCustomerName()) ? request.getCustomerName() : "Customer";
        String promo = StringUtils.hasText(request.getPromoCode()) ? request.getPromoCode() : "WELCOME100";

        if (StringUtils.hasText(request.getMessage())
                && (request.getType() == null || "text".equalsIgnoreCase(request.getType()))) {
            return request.getMessage();
        }

        return """
                Hello %s

                Welcome to Altitude Labs™

                Pure Himalayan Shilajit — lab-tested, altitude-harvested.

                ✅ Boost testosterone support
                ✅ Strength & stamina
                ✅ Faster recovery
                ✅ Natural energy

                Use Promo Code: %s
                Get ₹100 OFF on your first order.

                Shop now: https://www.altitudelabs.in/

                Reply INTERESTED to talk to a specialist
                Reply STOP to opt out
                """.formatted(name, promo).trim();
    }

    private void ensureConfigured(ResolvedCredentials creds) {
        if (!StringUtils.hasText(creds.getAccessToken()) || !StringUtils.hasText(creds.getPhoneNumberId())) {
            throw new BusinessException(
                    "WhatsApp is not configured. Set WHATSAPP_TOKEN and WHATSAPP_PHONE_NUMBER_ID "
                            + "(or company_whatsapp_config for the tenant).",
                    HttpStatus.SERVICE_UNAVAILABLE.value());
        }
    }

    private Map<String, Object> exchangeAndSave(
            WhatsAppApiPayload payload,
            String mobile,
            String savedMessage,
            String messageType) {
        ResolvedCredentials creds = credentialResolver.resolve();
        ensureConfigured(creds);

        String url = String.format(
                "%s/%s/%s/messages",
                GRAPH_API_BASE_URL,
                creds.getApiVersion(),
                creds.getPhoneNumberId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(creds.getAccessToken());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(payload, headers), Map.class);
            logWhatsAppApiResponse(response.getStatusCode().value(), response.getBody());
            conversationService.saveOutbound(
                    mobile, savedMessage, extractWaMessageId(response.getBody()), messageType, creds.getCompanyId());
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            log.error("WhatsApp API error: status={}, body={}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new WhatsAppApiException(ex.getResponseBodyAsString(), ex.getStatusCode().value());
        }
    }

    private void logWhatsAppApiResponse(int statusCode, Map<String, Object> body) {
        try {
            log.info("WhatsApp API response: status={}, body={}", statusCode, objectMapper.writeValueAsString(body));
        } catch (JsonProcessingException ex) {
            log.info("WhatsApp API response: status={}, body={}", statusCode, body);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractWaMessageId(Map<String, Object> body) {
        if (body == null || body.get("messages") == null) {
            return null;
        }
        Object messages = body.get("messages");
        if (messages instanceof java.util.List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> first) {
            Object id = first.get("id");
            return id != null ? id.toString() : null;
        }
        return null;
    }

    private static String normalizeMobile(String mobile) {
        if (mobile == null) {
            return null;
        }
        String digits = mobile.replaceAll("\\D", "");
        if (digits.length() == 10) {
            return "91" + digits;
        }
        return digits;
    }
}
