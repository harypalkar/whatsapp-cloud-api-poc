package com.whatsflow.security;

import com.whatsflow.tenant.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.parse(header.substring(7));
                UUID userId = UUID.fromString(claims.getSubject());
                UUID tenantId = UUID.fromString(String.valueOf(claims.get("tenantId")));
                String email = String.valueOf(claims.get("email"));
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles", List.class);
                Set<String> roleSet = roles == null ? Set.of() : new HashSet<>(roles);
                SecurityUser securityUser = new SecurityUser(userId, tenantId, email, "", true, roleSet);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities()));
                TenantContext.setTenantId(tenantId);
                TenantContext.setUserId(userId);
                TenantContext.setRoles(roleSet);
            } catch (Exception ignored) {
                // Invalid/expired token: continue as anonymous so public routes (register/login) still work.
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
