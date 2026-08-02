package com.whatsflow.company.api;

import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.company.dto.CompanyProfileRequest;
import com.whatsflow.company.dto.CompanyProfileResponse;
import com.whatsflow.company.service.CompanyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/company")
@Tag(name = "Company")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ApiResponse<CompanyProfileResponse> get() {
        return ApiResponse.ok(companyService.getCurrent());
    }

    @PutMapping
    public ApiResponse<CompanyProfileResponse> update(@Valid @RequestBody CompanyProfileRequest request) {
        return ApiResponse.ok(companyService.updateProfile(request), "Company profile saved");
    }
}
