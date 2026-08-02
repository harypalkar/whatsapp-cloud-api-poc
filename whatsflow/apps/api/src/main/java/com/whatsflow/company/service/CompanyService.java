package com.whatsflow.company.service;

import com.whatsflow.company.domain.Company;
import com.whatsflow.company.dto.CompanyProfileRequest;
import com.whatsflow.company.dto.CompanyProfileResponse;
import com.whatsflow.company.repository.CompanyRepository;
import com.whatsflow.exception.NotFoundException;
import com.whatsflow.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companies;

    public CompanyService(CompanyRepository companies) {
        this.companies = companies;
    }

    @Transactional(readOnly = true)
    public CompanyProfileResponse getCurrent() {
        return toDto(requireCurrent());
    }

    @Transactional
    public CompanyProfileResponse updateProfile(CompanyProfileRequest req) {
        Company company = requireCurrent();
        company.setName(req.name().trim());
        company.setGstin(blankToNull(req.gstin()));
        company.setAddressLine1(blankToNull(req.addressLine1()));
        company.setAddressLine2(blankToNull(req.addressLine2()));
        company.setCity(blankToNull(req.city()));
        company.setState(blankToNull(req.state()));
        company.setPincode(blankToNull(req.pincode()));
        if (req.timezone() != null && !req.timezone().isBlank()) {
            company.setTimezone(req.timezone());
        }
        company.setLogoUrl(blankToNull(req.logoUrl()));
        if (company.getOnboardingStep() < 1) {
            company.setOnboardingStep(1);
        }
        return toDto(companies.save(company));
    }

    @Transactional
    public CompanyProfileResponse selectPlan(String planCode) {
        Company company = requireCurrent();
        company.setPlanCode(planCode);
        if (company.getOnboardingStep() < 2) {
            company.setOnboardingStep(2);
        }
        return toDto(companies.save(company));
    }

    @Transactional
    public Company requireCurrent() {
        UUID tenantId = TenantContext.requireTenantId();
        return companies.findById(tenantId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new NotFoundException("Company not found"));
    }

    public CompanyProfileResponse toDto(Company c) {
        return new CompanyProfileResponse(
                c.getId(),
                c.getName(),
                c.getSlug(),
                c.getStatus(),
                c.getTimezone(),
                c.getGstin(),
                c.getAddressLine1(),
                c.getAddressLine2(),
                c.getCity(),
                c.getState(),
                c.getPincode(),
                c.getLogoUrl(),
                c.getPlanCode(),
                c.getOnboardingStep(),
                c.isOnboardingCompleted(),
                c.isWhatsappConnected()
        );
    }

    private static String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
