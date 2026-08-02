package com.whatsflow.company.dto;

import jakarta.validation.constraints.NotBlank;

public record CompanyProfileRequest(
        @NotBlank String name,
        String gstin,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String pincode,
        String timezone,
        String logoUrl
) {}
