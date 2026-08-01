package com.example.whatsapp.controller;

import com.example.whatsapp.dto.BulkCustomerOnboardRequest;
import com.example.whatsapp.dto.CustomerOnboardRequest;
import com.example.whatsapp.entity.Customer;
import com.example.whatsapp.service.CustomerOnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Onboarding", description = "Onboard customers and send Altitude Labs promos")
public class CustomerOnboardController {

    private final CustomerOnboardingService customerOnboardingService;

    @PostMapping("/onboard")
    @Operation(summary = "Onboard one customer and send WhatsApp welcome/promo")
    public ResponseEntity<Map<String, Object>> onboard(@Valid @RequestBody CustomerOnboardRequest request) {
        return ResponseEntity.ok(customerOnboardingService.onboard(request));
    }

    @PostMapping("/onboard/bulk")
    @Operation(summary = "Onboard multiple customers and send promos")
    public ResponseEntity<List<Map<String, Object>>> onboardBulk(
            @Valid @RequestBody BulkCustomerOnboardRequest request) {
        return ResponseEntity.ok(customerOnboardingService.onboardBulk(request));
    }

    @GetMapping
    @Operation(summary = "List onboarded customers")
    public ResponseEntity<List<Customer>> list() {
        return ResponseEntity.ok(customerOnboardingService.listCustomers());
    }
}
