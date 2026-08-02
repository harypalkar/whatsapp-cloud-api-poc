package com.whatsflow.webhook.api;


import com.whatsflow.webhook.service.WebhookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/webhooks/meta/whatsapp")
@Tag(name = "Meta Webhooks")
public class MetaWebhookController {
    private final WebhookService webhookService;
    public MetaWebhookController(WebhookService webhookService) { this.webhookService = webhookService; }

    @GetMapping
    public ResponseEntity<String> verify(@RequestParam(name = "hub.mode", required = false) String mode,
                                         @RequestParam(name = "hub.verify_token", required = false) String token,
                                         @RequestParam(name = "hub.challenge", required = false) String challenge) {
        if (webhookService.verify(mode, token, challenge)) return ResponseEntity.ok(challenge);
        return ResponseEntity.status(403).body("Forbidden");
    }

    @PostMapping
    public ResponseEntity<String> receive(@RequestBody String body) {
        webhookService.handle(body);
        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}
