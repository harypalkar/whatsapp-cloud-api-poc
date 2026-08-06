package com.whatsflow.dashboard.api;

import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.demo.service.DemoCatalogService;
import com.whatsflow.tenant.TenantContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/dashboard")
@Tag(name = "Dashboard")
public class DashboardController {

    private final DemoCatalogService demoCatalog;

    public DashboardController(DemoCatalogService demoCatalog) {
        this.demoCatalog = demoCatalog;
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        return ApiResponse.ok(demoCatalog.dashboard(TenantContext.requireTenantId()));
    }
}
