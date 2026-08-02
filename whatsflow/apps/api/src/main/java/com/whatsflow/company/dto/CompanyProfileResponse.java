package com.whatsflow.company.dto;

import java.util.UUID;

public record CompanyProfileResponse(
        UUID id,
        String name,
        String slug,
        String status,
        String timezone,
        String gstin,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String pincode,
        String logoUrl,
        String planCode,
        int onboardingStep,
        boolean onboardingCompleted,
        boolean whatsappConnected
) {}
