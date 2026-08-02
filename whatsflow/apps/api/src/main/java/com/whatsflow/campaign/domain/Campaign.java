package com.whatsflow.campaign.domain;


import com.whatsflow.common.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "campaigns")
public class Campaign extends TenantEntity {

    @Column(nullable = false) private String name;
    @Column(nullable = false, length = 32) private String status = "DRAFT";
    @Column(name = "template_name") private String templateName;
    private String language = "en";
    @Column(name = "promo_code") private String promoCode;
    @Column(name = "scheduled_at") private java.time.Instant scheduledAt;
    @Column(name = "recurring_cron") private String recurringCron;
    @Column(name = "whatsapp_account_id") private java.util.UUID whatsappAccountId;

}
