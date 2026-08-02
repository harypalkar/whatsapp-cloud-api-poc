package com.whatsflow.forms.api;


import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.forms.domain.FormDefinition;
import com.whatsflow.forms.repository.FormRepository;
import com.whatsflow.tenant.TenantContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/forms")
@Tag(name = "Forms")
public class FormController {
    private final FormRepository repo;
    public FormController(FormRepository repo) { this.repo = repo; }

    @GetMapping
    public ApiResponse<List<FormDefinition>> list() {
        return ApiResponse.ok(repo.findByTenantIdAndDeletedFalse(TenantContext.requireTenantId()));
    }

    @PostMapping
    public ApiResponse<FormDefinition> create(@RequestBody Map<String, String> body) {
        FormDefinition f = new FormDefinition();
        f.setTenantId(TenantContext.requireTenantId());
        f.setName(body.getOrDefault("name", "Untitled"));
        f.setFormType(body.getOrDefault("formType", "LEAD"));
        f.setPublicToken(UUID.randomUUID().toString().replace("-", ""));
        f.setStatus("DRAFT");
        f.setSchemaJson(body.getOrDefault("schemaJson", "[]"));
        return ApiResponse.ok(repo.save(f));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<FormDefinition> publish(@PathVariable UUID id) {
        FormDefinition f = repo.findById(id).orElseThrow();
        f.setStatus("PUBLISHED");
        return ApiResponse.ok(repo.save(f));
    }
}
