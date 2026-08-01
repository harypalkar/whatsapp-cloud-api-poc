package com.example.whatsapp.config;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request);

        log.info(
                "Request received: method={}, uri={}, query={}, remoteAddr={}, headers={}",
                wrapped.getMethod(),
                wrapped.getRequestURI(),
                wrapped.getQueryString(),
                wrapped.getRemoteAddr(),
                extractHeaders(wrapped));

        try {
            filterChain.doFilter(wrapped, response);
        } finally {
            if ("POST".equalsIgnoreCase(wrapped.getMethod())
                    && wrapped.getRequestURI() != null
                    && wrapped.getRequestURI().contains("webhook")) {
                byte[] buf = wrapped.getContentAsByteArray();
                if (buf.length > 0) {
                    String body = new String(buf, StandardCharsets.UTF_8);
                    log.info("Webhook request body: {}", body);
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info(
                    "Request completed: method={}, uri={}, status={}, durationMs={}",
                    wrapped.getMethod(),
                    wrapped.getRequestURI(),
                    response.getStatus(),
                    duration);
        }
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        if (names == null) {
            return headers;
        }
        for (String name : Collections.list(names)) {
            headers.put(name, request.getHeader(name));
        }
        return headers;
    }
}
