package com.example.whatsapp.service;

import com.example.whatsapp.config.WhatsAppProperties;
import com.example.whatsapp.entity.CompanyWhatsAppConfig;
import com.example.whatsapp.repository.CompanyWhatsAppConfigRepository;
import com.example.whatsapp.tenant.TenantContext;
import com.example.whatsapp.tenant.TenantFilter;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Resolves per-tenant WhatsApp credentials; falls back to application.yml defaults.
 */
@Component
@RequiredArgsConstructor
public class WhatsAppCredentialResolver {

    private final CompanyWhatsAppConfigRepository configRepository;
    private final WhatsAppProperties defaults;

    public ResolvedCredentials resolve() {
        final Long companyId = TenantContext.getCompanyId() != null
                ? TenantContext.getCompanyId()
                : TenantFilter.DEFAULT_COMPANY_ID;

        return configRepository.findByCompanyIdAndActiveTrue(companyId)
                .map(this::fromTenant)
                .orElseGet(() -> fromDefaults(companyId));
    }

    private ResolvedCredentials fromTenant(CompanyWhatsAppConfig config) {
        // Prefer application.yml / .env template name so Dev/Prod switch works without DB edits.
        return ResolvedCredentials.builder()
                .companyId(config.getCompanyId())
                .accessToken(firstNonBlank(config.getAccessToken(), defaults.getAccessToken()))
                .phoneNumberId(firstNonBlank(config.getPhoneNumberId(), defaults.getPhoneNumberId()))
                .businessAccountId(firstNonBlank(config.getBusinessAccountId(), defaults.getBusinessAccountId()))
                .apiVersion(firstNonBlank(config.getApiVersion(), defaults.getApiVersion()))
                .templateName(firstNonBlank(defaults.getResolvedTemplateName(), config.getTemplateName()))
                .templateLanguage(firstNonBlank(defaults.getResolvedTemplateLanguage(), config.getTemplateLanguage()))
                .displayPhoneNumber(firstNonBlank(config.getDisplayPhoneNumber(), defaults.getDisplayPhoneNumber()))
                .build();
    }

    private ResolvedCredentials fromDefaults(Long companyId) {
        return ResolvedCredentials.builder()
                .companyId(companyId)
                .accessToken(defaults.getAccessToken())
                .phoneNumberId(defaults.getPhoneNumberId())
                .businessAccountId(defaults.getBusinessAccountId())
                .apiVersion(defaults.getApiVersion())
                .templateName(defaults.getResolvedTemplateName())
                .templateLanguage(defaults.getResolvedTemplateLanguage())
                .displayPhoneNumber(defaults.getDisplayPhoneNumber())
                .build();
    }

    private static String firstNonBlank(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    @Getter
    @Builder
    public static class ResolvedCredentials {
        private final Long companyId;
        private final String accessToken;
        private final String phoneNumberId;
        private final String businessAccountId;
        private final String apiVersion;
        private final String templateName;
        private final String templateLanguage;
        private final String displayPhoneNumber;
    }
}
