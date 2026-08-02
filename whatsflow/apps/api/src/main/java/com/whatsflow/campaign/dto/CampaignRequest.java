package com.whatsflow.campaign.dto;


import jakarta.validation.constraints.NotBlank;
public record CampaignRequest(@NotBlank String name, String templateName, String language, String promoCode) {}
