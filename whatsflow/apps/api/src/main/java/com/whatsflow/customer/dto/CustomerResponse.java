package com.whatsflow.customer.dto;


import java.util.UUID;
public record CustomerResponse(UUID id, String mobileE164, String name, String email, boolean optedIn, boolean blacklisted) {}
