package com.whatsflow.demo.api;

import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.demo.config.DemoProperties;
import com.whatsflow.demo.service.DemoCatalogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/demo")
@Tag(name = "Demo Mode")
public class DemoController {

    private final DemoCatalogService catalog;
    private final DemoProperties props;

    public DemoController(DemoCatalogService catalog, DemoProperties props) {
        this.catalog = catalog;
        this.props = props;
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("enabled", props.isEnabled());
        m.put("email", props.getEmail());
        m.put("companyName", props.getCompanyName());
        m.put("hint", "Login with demo user when Demo Mode is enabled on the API.");
        return ApiResponse.ok(m);
    }

    @GetMapping("/catalog")
    public ApiResponse<Map<String, Object>> catalog() {
        return ApiResponse.ok(catalog.fullCatalog());
    }

    @GetMapping("/catalog/{module}")
    public ApiResponse<Object> module(@PathVariable String module) {
        Map<String, Object> all = catalog.fullCatalog();
        Object value = all.getOrDefault(module, Map.of("error", "Unknown module"));
        return ApiResponse.ok(value);
    }

    @PostMapping("/scenarios/{id}/run")
    public ApiResponse<Map<String, Object>> runScenario(@PathVariable String id) {
        return ApiResponse.ok(Map.of(
                "scenarioId", id,
                "status", "EXECUTED",
                "steps", List.of(
                        "Loaded demo audience segment",
                        "Selected approved WhatsApp template",
                        "Queued mock provider sends",
                        "Updated campaign statistics",
                        "Created inbox follow-up threads"
                ),
                "message", "Demo scenario executed successfully using mock WhatsApp provider."
        ));
    }
}
