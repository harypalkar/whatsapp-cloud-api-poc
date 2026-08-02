package com.whatsflow.onboarding.api;

import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.company.dto.CompanyProfileResponse;
import com.whatsflow.onboarding.service.OnboardingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/onboarding")
@Tag(name = "Onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status() {
        return ApiResponse.ok(onboardingService.status());
    }

    @PostMapping("/plan")
    public ApiResponse<CompanyProfileResponse> selectPlan(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(onboardingService.selectPlan(body.get("planCode")), "Plan selected");
    }

    @PostMapping("/meta/complete")
    public ApiResponse<Map<String, Object>> metaComplete(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(onboardingService.completeMetaSignup(body), "Meta account linked");
    }

    @PostMapping("/whatsapp/connect")
    public ApiResponse<Map<String, Object>> connectWhatsApp(@RequestBody(required = false) Map<String, String> body) {
        return ApiResponse.ok(onboardingService.connectWhatsApp(body == null ? Map.of() : body), "WhatsApp connected");
    }

    @PostMapping("/finish")
    public ApiResponse<CompanyProfileResponse> finish() {
        return ApiResponse.ok(onboardingService.finish(), "Onboarding complete");
    }

    @PostMapping("/skip")
    public ApiResponse<CompanyProfileResponse> skip() {
        return ApiResponse.ok(onboardingService.skip(), "Onboarding skipped");
    }
}
