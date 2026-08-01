package com.example.whatsapp.controller;

import com.example.whatsapp.dto.BulkSendTemplateRequest;
import com.example.whatsapp.dto.SendMessageV1Request;
import com.example.whatsapp.dto.SendTemplateRequest;
import com.example.whatsapp.service.WhatsAppCredentialResolver;
import com.example.whatsapp.service.WhatsAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Messages V1", description = "Multi-tenant WhatsApp outbound messaging")
public class MessageV1Controller {

    private final WhatsAppService whatsAppService;
    private final WhatsAppCredentialResolver credentialResolver;

    @PostMapping("/send-template")
    @Operation(
            summary = "Send marketing template (altitude_welcome_promo)",
            description = "Cold outreach to ANY mobile — no customer Hi required. Body vars: customerName, promoCode.")
    public ResponseEntity<Map<String, Object>> sendTemplate(@Valid @RequestBody SendTemplateRequest request) {
        return ResponseEntity.ok(buildTemplateResponse(request));
    }

    @PostMapping("/send-template/bulk")
    @Operation(summary = "Bulk cold promo to many mobiles (marketing template)")
    public ResponseEntity<Map<String, Object>> sendTemplateBulk(@Valid @RequestBody BulkSendTemplateRequest request) {
        List<Map<String, Object>> results = new ArrayList<>();
        int sent = 0;
        int failed = 0;
        for (SendTemplateRequest recipient : request.resolvedRecipients()) {
            try {
                results.add(buildTemplateResponse(recipient));
                sent++;
            } catch (Exception ex) {
                failed++;
                Map<String, Object> err = new HashMap<>();
                err.put("status", "FAILED");
                err.put("receiver", recipient.getMobile());
                err.put("customerName", recipient.getCustomerName());
                err.put("error", ex.getMessage());
                results.add(err);
            }
        }
        Map<String, Object> body = new HashMap<>();
        body.put("sent", sent);
        body.put("failed", failed);
        body.put("total", sent + failed);
        body.put("results", results);
        return ResponseEntity.ok(body);
    }

    private Map<String, Object> buildTemplateResponse(SendTemplateRequest request) {
        var creds = credentialResolver.resolve();
        Map<String, Object> metaResponse = whatsAppService.sendPromoTemplate(request);

        Map<String, Object> body = new HashMap<>();
        body.put("status", "SENT");
        body.put("companyId", creds.getCompanyId());
        body.put("sender", creds.getDisplayPhoneNumber());
        body.put("receiver", request.getMobile());
        body.put("customerName", request.getCustomerName());
        body.put("promoCode", request.getPromoCode());
        body.put("templateName", creds.getTemplateName());
        body.put("templateLanguage", creds.getTemplateLanguage());
        body.put("type", "template");
        body.put("meta", metaResponse);
        return body;
    }

    @PostMapping("/send")
    @Operation(summary = "Send WhatsApp message (text / template / media / location / buttons)")
    public ResponseEntity<Map<String, Object>> send(@Valid @RequestBody SendMessageV1Request request) {
        var creds = credentialResolver.resolve();
        Map<String, Object> metaResponse = whatsAppService.sendV1(request);

        Map<String, Object> body = new HashMap<>();
        body.put("status", "SENT");
        body.put("companyId", creds.getCompanyId());
        body.put("sender", creds.getDisplayPhoneNumber());
        body.put("receiver", request.getCustomerNumber());
        body.put("customerName", request.getCustomerName());
        body.put("promoCode", request.getPromoCode());
        body.put("type", request.getType() != null ? request.getType() : "text");
        body.put("meta", metaResponse);
        return ResponseEntity.ok(body);
    }
}
