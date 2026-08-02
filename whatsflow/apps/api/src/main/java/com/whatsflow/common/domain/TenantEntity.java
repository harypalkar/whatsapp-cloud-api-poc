package com.whatsflow.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class TenantEntity extends BaseAuditableEntity implements TenantAware {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
}
