package com.whatsflow.company.domain;

import com.whatsflow.common.domain.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "companies")
public class Company extends BaseAuditableEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true, length = 128)
    private String slug;

    @Column(nullable = false, length = 50)
    private String status = "ACTIVE";

    private String timezone = "Asia/Kolkata";

    @Column(length = 32)
    private String gstin;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    private String city;
    private String state;

    @Column(length = 16)
    private String pincode;

    @Column(name = "logo_url", length = 1024)
    private String logoUrl;

    @Column(name = "plan_code", length = 64)
    private String planCode;

    /** 0=registered, 1=details, 2=plan, 3=meta, 4=whatsapp, 5=done */
    @Column(name = "onboarding_step", nullable = false)
    private int onboardingStep = 0;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted = false;

    @Column(name = "whatsapp_connected", nullable = false)
    private boolean whatsappConnected = false;
}
