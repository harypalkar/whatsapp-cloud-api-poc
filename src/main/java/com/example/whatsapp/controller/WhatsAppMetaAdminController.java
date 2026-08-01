package com.example.whatsapp.controller;

import com.example.whatsapp.service.WhatsAppMetaAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/meta")
@RequiredArgsConstructor
@Tag(name = "Meta Admin", description = "Discover WABA phone profile and approved templates")
public class WhatsAppMetaAdminController {

    private final WhatsAppMetaAdminService metaAdminService;

    @GetMapping("/phone")
    @Operation(summary = "Show connected business phone / WABA")
    public ResponseEntity<Map<String, Object>> phone() {
        return ResponseEntity.ok(metaAdminService.phoneProfile());
    }

    @GetMapping("/templates")
    @Operation(summary = "List message templates on the WABA (find approved cold-promo name/language)")
    public ResponseEntity<Map<String, Object>> templates() {
        return ResponseEntity.ok(metaAdminService.listMessageTemplates());
    }
}
