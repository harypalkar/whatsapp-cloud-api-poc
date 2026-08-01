package com.example.whatsapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Health", description = "Application health endpoints")
public class HealthController {

    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Health check", description = "Returns plain-text application status")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body("Application Running");
    }

    @GetMapping("/api/health")
    @Operation(summary = "JSON health check")
    public ResponseEntity<Map<String, String>> healthJson() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
