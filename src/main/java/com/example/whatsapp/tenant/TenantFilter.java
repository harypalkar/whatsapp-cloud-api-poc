package com.example.whatsapp.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Resolves tenant from {@code X-Company-Id} header. When absent, default company (Altitude Labs = 1) is used.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class TenantFilter extends OncePerRequestFilter {

    public static final String COMPANY_HEADER = "X-Company-Id";
    public static final long DEFAULT_COMPANY_ID = 1L;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            Long companyId = DEFAULT_COMPANY_ID;
            String header = request.getHeader(COMPANY_HEADER);
            if (header != null && !header.isBlank()) {
                try {
                    companyId = Long.parseLong(header.trim());
                } catch (NumberFormatException ignored) {
                    companyId = DEFAULT_COMPANY_ID;
                }
            }
            TenantContext.setCompanyId(companyId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
