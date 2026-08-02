package com.whatsflow.tenant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TenantContext {

    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<UUID> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> ROLES = ThreadLocal.withInitial(HashSet::new);

    public static void setTenantId(UUID tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static Optional<UUID> getTenantId() {
        return Optional.ofNullable(TENANT_ID.get());
    }

    public static UUID requireTenantId() {
        return getTenantId().orElseThrow(() ->
                new IllegalStateException("Tenant context is not set"));
    }

    public static void setUserId(UUID userId) {
        USER_ID.set(userId);
    }

    public static Optional<UUID> getUserId() {
        return Optional.ofNullable(USER_ID.get());
    }

    public static void setRoles(Set<String> roles) {
        ROLES.set(roles != null ? new HashSet<>(roles) : new HashSet<>());
    }

    public static Set<String> getRoles() {
        Set<String> roles = ROLES.get();
        return roles != null ? Collections.unmodifiableSet(roles) : Set.of();
    }

    public static boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    public static void clear() {
        TENANT_ID.remove();
        USER_ID.remove();
        ROLES.remove();
    }
}
