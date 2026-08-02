package com.whatsflow.whatsapp.domain;

import com.whatsflow.common.domain.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "whatsapp_accounts")
public class WhatsAppAccount extends TenantEntity {

    @Column(name = "waba_id", length = 64)
    private String wabaId;

    @Column(name = "phone_number_id", nullable = false, length = 64)
    private String phoneNumberId;

    @Column(name = "business_id", length = 64)
    private String businessId;

    @Column(name = "display_phone", length = 32)
    private String displayPhone;

    @Column(name = "verified_name")
    private String verifiedName;

    @Column(name = "access_token_enc", columnDefinition = "TEXT")
    private String accessTokenEnc;

    @Column(name = "webhook_verify_token")
    private String webhookVerifyToken;

    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";
}
