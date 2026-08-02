package com.whatsflow.tenant;

import com.whatsflow.security.SecurityUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final TenantResolver tenantResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            populateFromSecurityContext();
            tenantResolver.resolve(request).ifPresent(TenantContext::setTenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private void populateFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser securityUser) {
            if (securityUser.getTenantId() != null) {
                TenantContext.setTenantId(securityUser.getTenantId());
            }
            TenantContext.setUserId(securityUser.getId());

            Set<String> roles = securityUser.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .filter(a -> a.startsWith("ROLE_"))
                    .collect(Collectors.toSet());
            TenantContext.setRoles(roles);
        }
    }
}
