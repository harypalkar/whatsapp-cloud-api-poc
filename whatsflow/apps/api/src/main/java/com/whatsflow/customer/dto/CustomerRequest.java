package com.whatsflow.customer.dto;


import jakarta.validation.constraints.NotBlank;
public record CustomerRequest(@NotBlank String mobileE164, String name, String email, Boolean optedIn) {}
