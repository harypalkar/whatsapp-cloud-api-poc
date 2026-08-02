package com.whatsflow.tenant;

import com.whatsflow.exception.ErrorCode;
import com.whatsflow.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String API_PREFIX = "/api/v1/";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();

        if (!path.startsWith(API_PREFIX)) {
            return true;
        }

        if (isPublicPath(path)) {
            return true;
        }

        if (TenantContext.getTenantId().isEmpty()) {
            log.warn("Missing tenant context for protected path: {}", path);
            throw new UnauthorizedException(ErrorCode.TENANT_REQUIRED, "Tenant context is required");
        }

        return true;
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/api/v1/webhooks/")
                || path.startsWith("/api/v1/public/");
    }
}
