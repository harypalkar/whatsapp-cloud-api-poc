package com.whatsflow.identity.dto;


import java.util.List;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        UUID tenantId,
        String email,
        List<String> roles
) {}
