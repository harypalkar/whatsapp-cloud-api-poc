package com.whatsflow.campaign.api;


import com.whatsflow.campaign.domain.Campaign;
import com.whatsflow.campaign.dto.CampaignRequest;
import com.whatsflow.campaign.service.CampaignService;
import com.whatsflow.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/campaigns")
@Tag(name = "Campaigns")
public class CampaignController {
    private final CampaignService service;
    public CampaignController(CampaignService service) { this.service = service; }

    @GetMapping public ApiResponse<Page<Campaign>> list(Pageable pageable) { return ApiResponse.ok(service.list(pageable)); }
    @PostMapping public ApiResponse<Campaign> create(@Valid @RequestBody CampaignRequest req) { return ApiResponse.ok(service.create(req)); }
    @PostMapping("/{id}/schedule") public ApiResponse<Campaign> schedule(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(service.schedule(id, Instant.parse(body.get("scheduledAt"))));
    }
    @PostMapping("/{id}/pause") public ApiResponse<Campaign> pause(@PathVariable UUID id) { return ApiResponse.ok(service.transition(id, "PAUSED")); }
    @PostMapping("/{id}/resume") public ApiResponse<Campaign> resume(@PathVariable UUID id) { return ApiResponse.ok(service.transition(id, "RUNNING")); }
    @PostMapping("/{id}/cancel") public ApiResponse<Campaign> cancel(@PathVariable UUID id) { return ApiResponse.ok(service.transition(id, "CANCELLED")); }
}
