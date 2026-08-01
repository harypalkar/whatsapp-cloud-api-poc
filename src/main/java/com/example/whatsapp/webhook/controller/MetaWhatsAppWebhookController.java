package com.example.whatsapp.webhook.controller;

import com.example.whatsapp.dto.WhatsAppWebhookPayload;
import com.example.whatsapp.webhook.service.MetaWebhookService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Meta WhatsApp Cloud API webhook endpoints.
 * <p>
 * Primary callback (configured in Meta / ngrok):
 * {@code https://altitudelabs.ngrok-free.app/webhook/whatsapp}
 * <p>
 * Spec path also exposed:
 * {@code /api/v1/webhooks/meta/whatsapp}
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Meta WhatsApp Webhook")
public class MetaWhatsAppWebhookController {

    private final MetaWebhookService metaWebhookService;

    @GetMapping(
            value = {"/webhook/whatsapp", "/api/v1/webhooks/meta/whatsapp"},
            produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Meta webhook verification (returns hub.challenge only)")
    public ResponseEntity<String> verify(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String verifyToken,
            @RequestParam(value = "hub.challenge", required = false) String challenge,
            HttpServletRequest request) {

        logRequest("GET", request);
        String body = metaWebhookService.verifySubscription(mode, verifyToken, challenge);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(body);
    }

    @PostMapping(
            value = {"/webhook/whatsapp", "/api/v1/webhooks/meta/whatsapp"},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    @Hidden
    public ResponseEntity<String> receive(
            @RequestBody WhatsAppWebhookPayload payload,
            HttpServletRequest request) {

        logRequest("POST", request);
        String body = metaWebhookService.ingest(payload);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(body);
    }

    private void logRequest(String method, HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            for (String name : Collections.list(headerNames)) {
                headers.put(name, request.getHeader(name));
            }
        }

        Map<String, String[]> params = request.getParameterMap();
        log.info(
                "Meta webhook {} uri={} queryString={} headers={} params={}",
                method,
                request.getRequestURI(),
                request.getQueryString(),
                headers,
                params.keySet());
    }
}
