package com.whatsflow.customer.api;


import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.common.api.PageResponse;
import com.whatsflow.customer.dto.CustomerRequest;
import com.whatsflow.customer.dto.CustomerResponse;
import com.whatsflow.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/customers")
@Tag(name = "Customers")
public class CustomerController {
    private final CustomerService service;
    public CustomerController(CustomerService service) { this.service = service; }

    @GetMapping
    public ApiResponse<PageResponse<CustomerResponse>> list(@RequestParam(required = false) String q, Pageable pageable) {
        return ApiResponse.ok(service.list(q, pageable));
    }
    @PostMapping
    public ApiResponse<CustomerResponse> create(@Valid @RequestBody CustomerRequest req) {
        return ApiResponse.ok(service.create(req));
    }
    @GetMapping("/{id}")
    public ApiResponse<CustomerResponse> get(@PathVariable UUID id) { return ApiResponse.ok(service.get(id)); }
    @PutMapping("/{id}")
    public ApiResponse<CustomerResponse> update(@PathVariable UUID id, @Valid @RequestBody CustomerRequest req) {
        return ApiResponse.ok(service.update(id, req));
    }
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) { service.softDelete(id); return ApiResponse.ok(null, "Deleted"); }
    @PostMapping("/{id}/opt-in")
    public ApiResponse<CustomerResponse> optIn(@PathVariable UUID id) { return ApiResponse.ok(service.optIn(id, true)); }
    @PostMapping("/{id}/opt-out")
    public ApiResponse<CustomerResponse> optOut(@PathVariable UUID id) { return ApiResponse.ok(service.optIn(id, false)); }
    @PostMapping("/{id}/blacklist")
    public ApiResponse<CustomerResponse> blacklist(@PathVariable UUID id) { return ApiResponse.ok(service.blacklist(id, true)); }
}
