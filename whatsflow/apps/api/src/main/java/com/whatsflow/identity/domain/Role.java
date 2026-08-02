package com.whatsflow.identity.domain;


import com.whatsflow.common.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "roles")
public class Role extends TenantEntity {
    @Column(nullable = false, length = 100) private String code;
    @Column(nullable = false) private String name;
}
