package com.whatsflow.identity.domain;


import com.whatsflow.common.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "users")
public class UserAccount extends TenantEntity {
    @Column(nullable = false, length = 320) private String email;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Column(name = "full_name") private String fullName;
    @Column(name = "email_verified", nullable = false) private boolean emailVerified = false;
    @Column(nullable = false) private boolean enabled = true;
}
