package com.whatsflow.reports.api;


import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.customer.repository.CustomerRepository;
import com.whatsflow.tenant.TenantContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/v1/reports")
@Tag(name = "Reports")
public class ReportController {
    private final CustomerRepository customers;
    public ReportController(CustomerRepository customers) { this.customers = customers; }

    @GetMapping("/customers")
    public ApiResponse<Map<String, Object>> customers() {
        long total = customers.countByTenantIdAndDeletedFalse(TenantContext.requireTenantId());
        return ApiResponse.ok(Map.of("totalCustomers", total));
    }
}
