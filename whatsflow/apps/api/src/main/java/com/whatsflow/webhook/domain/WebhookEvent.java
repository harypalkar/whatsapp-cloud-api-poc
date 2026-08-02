package com.whatsflow.webhook.domain;


import com.whatsflow.common.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter @Setter
@Entity
@Table(name = "webhook_events")
public class WebhookEvent extends BaseAuditableEntity {
    @Column(name = "tenant_id") private UUID tenantId;
    @Column(name = "external_id") private String externalId;
    @Column(name = "event_type") private String eventType;
    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT") private String payloadJson;
    @Column(name = "process_status", nullable = false) private String processStatus = "RECEIVED";
}
