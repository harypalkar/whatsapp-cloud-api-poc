package com.whatsflow.onboarding.service;

import com.whatsflow.common.crypto.AesGcmEncryptor;
import com.whatsflow.company.domain.Company;
import com.whatsflow.company.dto.CompanyProfileResponse;
import com.whatsflow.company.repository.CompanyRepository;
import com.whatsflow.company.service.CompanyService;
import com.whatsflow.config.WhatsAppProperties;
import com.whatsflow.exception.BusinessException;
import com.whatsflow.exception.ErrorCode;
import com.whatsflow.tenant.TenantContext;
import com.whatsflow.whatsapp.domain.WhatsAppAccount;
import com.whatsflow.whatsapp.repository.WhatsAppAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OnboardingService {

    private final CompanyService companyService;
    private final CompanyRepository companies;
    private final WhatsAppAccountRepository whatsAppAccounts;
    private final AesGcmEncryptor encryptor;
    private final WhatsAppProperties whatsAppProperties;

    public OnboardingService(CompanyService companyService,
                             CompanyRepository companies,
                             WhatsAppAccountRepository whatsAppAccounts,
                             AesGcmEncryptor encryptor,
                             WhatsAppProperties whatsAppProperties) {
        this.companyService = companyService;
        this.companies = companies;
        this.whatsAppAccounts = whatsAppAccounts;
        this.encryptor = encryptor;
        this.whatsAppProperties = whatsAppProperties;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> status() {
        CompanyProfileResponse company = companyService.getCurrent();
        Map<String, Object> result = new HashMap<>();
        result.put("company", company);
        result.put("currentStep", company.onboardingStep());
        result.put("completed", company.onboardingCompleted());
        result.put("steps", new String[]{
                "BUSINESS_DETAILS",
                "PLAN",
                "META_SIGNUP",
                "WHATSAPP_CONNECT",
                "SUCCESS"
        });
        whatsAppAccounts.findByTenantIdAndDeletedFalse(TenantContext.requireTenantId())
                .ifPresent(acc -> result.put("whatsappAccount", Map.of(
                        "phoneNumberId", acc.getPhoneNumberId(),
                        "displayPhone", acc.getDisplayPhone() == null ? "" : acc.getDisplayPhone(),
                        "wabaId", acc.getWabaId() == null ? "" : acc.getWabaId(),
                        "verifiedName", acc.getVerifiedName() == null ? "" : acc.getVerifiedName(),
                        "status", acc.getStatus()
                )));
        return result;
    }

    @Transactional
    public CompanyProfileResponse selectPlan(String planCode) {
        if (planCode == null || planCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Plan code is required");
        }
        return companyService.selectPlan(planCode.trim().toUpperCase());
    }

    @Transactional
    public Map<String, Object> completeMetaSignup(Map<String, String> body) {
        UUID tenantId = TenantContext.requireTenantId();
        Company company = companyService.requireCurrent();

        String phoneNumberId = firstNonBlank(body.get("phoneNumberId"), whatsAppProperties.getPhoneNumberId(), "1226308087231072");
        String wabaId = firstNonBlank(body.get("wabaId"), whatsAppProperties.getBusinessAccountId(), "1583394760167591");
        String displayPhone = firstNonBlank(body.get("displayPhone"), "+91 95126 18333");
        String verifiedName = firstNonBlank(body.get("verifiedName"), company.getName());
        String accessToken = firstNonBlank(body.get("accessToken"), whatsAppProperties.getAccessToken(), "mock-local-token");

        WhatsAppAccount account = whatsAppAccounts.findByTenantIdAndDeletedFalse(tenantId)
                .orElseGet(WhatsAppAccount::new);
        account.setTenantId(tenantId);
        account.setPhoneNumberId(phoneNumberId);
        account.setWabaId(wabaId);
        account.setBusinessId(body.getOrDefault("businessId", ""));
        account.setDisplayPhone(displayPhone);
        account.setVerifiedName(verifiedName);
        account.setAccessTokenEnc(encryptor.encrypt(accessToken));
        account.setWebhookVerifyToken(firstNonBlank(body.get("verifyToken"), whatsAppProperties.getVerifyToken()));
        account.setStatus("ACTIVE");
        whatsAppAccounts.save(account);

        if (company.getOnboardingStep() < 3) {
            company.setOnboardingStep(3);
        }
        companies.save(company);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "META_LINKED");
        result.put("phoneNumberId", phoneNumberId);
        result.put("wabaId", wabaId);
        result.put("displayPhone", displayPhone);
        result.put("company", companyService.toDto(company));
        return result;
    }

    @Transactional
    public Map<String, Object> connectWhatsApp(Map<String, String> body) {
        UUID tenantId = TenantContext.requireTenantId();
        Company company = companyService.requireCurrent();

        WhatsAppAccount account = whatsAppAccounts.findByTenantIdAndDeletedFalse(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_RULE,
                        "Complete Meta Embedded Signup before connecting WhatsApp"));

        if (body.get("displayPhone") != null && !body.get("displayPhone").isBlank()) {
            account.setDisplayPhone(body.get("displayPhone").trim());
        }
        if (body.get("verifiedName") != null && !body.get("verifiedName").isBlank()) {
            account.setVerifiedName(body.get("verifiedName").trim());
        }
        account.setStatus("CONNECTED");
        whatsAppAccounts.save(account);

        company.setWhatsappConnected(true);
        if (company.getOnboardingStep() < 4) {
            company.setOnboardingStep(4);
        }
        companies.save(company);

        return Map.of(
                "status", "WHATSAPP_CONNECTED",
                "displayPhone", account.getDisplayPhone() == null ? "" : account.getDisplayPhone(),
                "phoneNumberId", account.getPhoneNumberId(),
                "company", companyService.toDto(company)
        );
    }

    @Transactional
    public CompanyProfileResponse finish() {
        Company company = companyService.requireCurrent();
        if (company.getPlanCode() == null || company.getPlanCode().isBlank()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE, "Select a plan before finishing onboarding");
        }
        if (!company.isWhatsappConnected()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE, "Connect WhatsApp before finishing onboarding");
        }
        company.setOnboardingStep(5);
        company.setOnboardingCompleted(true);
        company.setStatus("ACTIVE");
        return companyService.toDto(companies.save(company));
    }

    /** Local/demo escape hatch so Skip does not bounce on the onboarding guard. */
    @Transactional
    public CompanyProfileResponse skip() {
        Company company = companyService.requireCurrent();
        if (company.getPlanCode() == null || company.getPlanCode().isBlank()) {
            company.setPlanCode("GROWTH");
        }
        company.setOnboardingStep(5);
        company.setOnboardingCompleted(true);
        company.setStatus("ACTIVE");
        return companyService.toDto(companies.save(company));
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return "";
    }
}
