package com.whatsflow.dashboard.api;


import com.whatsflow.campaign.repository.CampaignRepository;
import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.customer.repository.CustomerRepository;
import com.whatsflow.tenant.TenantContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/dashboard")
@Tag(name = "Dashboard")
public class DashboardController {
    private final CustomerRepository customers;
    private final CampaignRepository campaigns;

    public DashboardController(CustomerRepository customers, CampaignRepository campaigns) {
        this.customers = customers; this.campaigns = campaigns;
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        UUID tenantId = TenantContext.requireTenantId();
        long customerCount = customers.countByTenantIdAndDeletedFalse(tenantId);
        long campaignCount = campaigns.findByTenantIdAndDeletedFalse(tenantId, Pageable.unpaged()).getTotalElements();
        return ApiResponse.ok(Map.of(
                "customers", customerCount,
                "campaigns", campaignCount,
                "conversations", 0,
                "messagesToday", 0
        ));
    }
}
