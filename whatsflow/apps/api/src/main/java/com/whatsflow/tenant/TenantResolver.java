package com.whatsflow.tenant;

import com.whatsflow.exception.ErrorCode;
import com.whatsflow.exception.UnauthorizedException;
import com.whatsflow.security.SecurityUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class TenantResolver {

    public static final String TENANT_HEADER = "X-Tenant-Id";
    private static final String PLATFORM_ADMIN_ROLE = "ROLE_PLATFORM_ADMIN";

    public Optional<UUID> resolve(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser securityUser) {
            UUID jwtTenantId = securityUser.getTenantId();

            String headerTenantId = request.getHeader(TENANT_HEADER);
            if (headerTenantId != null && !headerTenantId.isBlank()) {
                if (!securityUser.getAuthorities().stream()
                        .anyMatch(a -> PLATFORM_ADMIN_ROLE.equals(a.getAuthority()))) {
                    throw new UnauthorizedException(ErrorCode.FORBIDDEN,
                            "X-Tenant-Id header is only allowed for platform administrators");
                }
                try {
                    UUID overrideTenantId = UUID.fromString(headerTenantId);
                    log.debug("Platform admin overriding tenant to {}", overrideTenantId);
                    return Optional.of(overrideTenantId);
                } catch (IllegalArgumentException ex) {
                    throw new UnauthorizedException(ErrorCode.VALIDATION_ERROR, "Invalid X-Tenant-Id header");
                }
            }

            return Optional.ofNullable(jwtTenantId);
        }

        return Optional.empty();
    }
}
