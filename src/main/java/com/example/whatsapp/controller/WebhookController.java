package com.example.whatsapp.controller;

import com.example.whatsapp.dto.WhatsAppWebhookPayload;
import com.example.whatsapp.webhook.service.MetaWebhookService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Legacy alias at /api/webhook. Prefer /webhook/whatsapp for Meta Embedded Signup / ngrok.
 */
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Hidden
public class WebhookController {

    private final MetaWebhookService metaWebhookService;

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String verifyToken,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {
        String body = metaWebhookService.verifySubscription(mode, verifyToken, challenge);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(body);
    }

    @PostMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> receiveWebhook(@RequestBody WhatsAppWebhookPayload payload) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(metaWebhookService.ingest(payload));
    }
}
