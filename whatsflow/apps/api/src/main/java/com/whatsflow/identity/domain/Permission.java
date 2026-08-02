package com.whatsflow.identity.domain;


import com.whatsflow.common.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "permissions")
public class Permission extends BaseAuditableEntity {
    @Column(nullable = false, unique = true, length = 150) private String code;
    private String description;
}
