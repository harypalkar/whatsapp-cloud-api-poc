package com.whatsflow.meta.api;

import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.config.WhatsFlowProperties;
import com.whatsflow.onboarding.service.OnboardingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/meta/embedded-signup")
@Tag(name = "Meta Embedded Signup")
public class EmbeddedSignupController {

    private final WhatsFlowProperties props;
    private final OnboardingService onboardingService;

    public EmbeddedSignupController(WhatsFlowProperties props, OnboardingService onboardingService) {
        this.props = props;
        this.onboardingService = onboardingService;
    }

    @PostMapping("/start")
    public ApiResponse<Map<String, Object>> start() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("appId", nullToEmpty(props.getMeta().getAppId()));
        payload.put("configId", nullToEmpty(props.getMeta().getConfigId()));
        payload.put("graphVersion", props.getMeta().getGraphApiVersion());
        payload.put("redirectUri", props.getMeta().getRedirectUri());
        payload.put("state", UUID.randomUUID().toString());
        payload.put("mode", nullToEmpty(props.getMeta().getAppId()).isBlank() ? "SIMULATED" : "LIVE");
        payload.put("hint", "In local/dev without Meta app credentials, use /complete with simulated values.");
        return ApiResponse.ok(payload);
    }

    @PostMapping("/complete")
    public ApiResponse<Map<String, Object>> complete(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(onboardingService.completeMetaSignup(body), "Embedded signup accepted");
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v;
    }
}
