package com.whatsflow.billing.api;

import com.whatsflow.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/billing/plans")
@Tag(name = "Billing")
public class PlanController {

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> plans() {
        return ApiResponse.ok(List.of(
                plan("STARTER", "Starter", 3, 5_000, 999, "For solo founders testing WhatsApp outreach"),
                plan("GROWTH", "Growth", 10, 50_000, 4_999, "For growing teams and campaigns"),
                plan("PROFESSIONAL", "Professional", 50, 250_000, 14_999, "Multi-agent inbox + automations"),
                plan("ENTERPRISE", "Enterprise", 500, 10_000_000, 49_999, "SLA, white-label, dedicated support")
        ));
    }

    private static Map<String, Object> plan(String code, String name, int agents, int msgs, int price, String desc) {
        return Map.of(
                "code", code,
                "name", name,
                "maxAgents", agents,
                "maxMessagesMonth", msgs,
                "priceMonthly", price,
                "currency", "INR",
                "description", desc
        );
    }
}
