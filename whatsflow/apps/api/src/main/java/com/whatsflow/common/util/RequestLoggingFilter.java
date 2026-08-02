package com.whatsflow.common.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/actuator/health",
            "/actuator/prometheus"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.currentTimeMillis();
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("{} {} -> {} ({} ms)",
                    wrappedRequest.getMethod(),
                    wrappedRequest.getRequestURI(),
                    wrappedResponse.getStatus(),
                    duration);

            if (log.isDebugEnabled() && isJsonRequest(wrappedRequest)) {
                log.debug("Request body: {}", getPayload(wrappedRequest.getContentAsByteArray()));
            }

            wrappedResponse.copyBodyToResponse();
        }
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.contains("application/json");
    }

    private String getPayload(byte[] content) {
        if (content.length == 0) {
            return "";
        }
        return new String(content, StandardCharsets.UTF_8);
    }
}
