package com.whatsflow.identity.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String companyName,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        String fullName
) {}
