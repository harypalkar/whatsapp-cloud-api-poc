package com.whatsflow.identity.domain;


import com.whatsflow.common.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends TenantEntity {
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "token_hash", nullable = false, unique = true) private String tokenHash;
    @Column(name = "family_id", nullable = false) private UUID familyId;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    private boolean revoked = false;
}
