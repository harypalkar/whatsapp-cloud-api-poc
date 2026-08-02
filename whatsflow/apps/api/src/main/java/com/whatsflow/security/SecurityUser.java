package com.whatsflow.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class SecurityUser implements UserDetails {

    private final UUID id;
    private final UUID tenantId;
    private final String email;
    private final String passwordHash;
    private final boolean enabled;
    private final Set<String> roles;

    public SecurityUser(UUID id, UUID tenantId, String email, String passwordHash,
                        boolean enabled, Set<String> roles) {
        this.id = id;
        this.tenantId = tenantId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority(r.startsWith("ROLE_") ? r : "ROLE_" + r))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
