package com.whatsflow.whatsapp.api;

import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.demo.service.DemoCatalogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/whatsapp")
@Tag(name = "WhatsApp")
public class WhatsAppAccountController {

    private final DemoCatalogService catalog;

    public WhatsAppAccountController(DemoCatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/account")
    public ApiResponse<Object> account() {
        Object wa = catalog.fullCatalog().get("whatsapp");
        return ApiResponse.ok(wa instanceof Map ? wa : Map.of());
    }
}
