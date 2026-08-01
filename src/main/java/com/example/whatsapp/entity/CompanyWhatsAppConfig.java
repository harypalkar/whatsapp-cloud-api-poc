package com.example.whatsapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_whatsapp_config")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyWhatsAppConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false, unique = true)
    private Long companyId;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "phone_number_id", length = 64)
    private String phoneNumberId;

    @Column(name = "business_account_id", length = 64)
    private String businessAccountId;

    @Column(name = "verify_token", length = 255)
    private String verifyToken;

    @Column(name = "template_name", nullable = false, length = 255)
    private String templateName;

    @Column(name = "template_language", nullable = false, length = 20)
    private String templateLanguage;

    @Column(name = "api_version", nullable = false, length = 20)
    private String apiVersion;

    @Column(name = "display_phone_number", length = 32)
    private String displayPhoneNumber;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (active == null) {
            active = true;
        }
    }
}
