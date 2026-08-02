package com.whatsflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource(WhatsFlowProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        String[] origins = properties.getCors().getAllowedOrigins();
        if (origins == null || origins.length == 0) {
            config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        } else {
            config.setAllowedOrigins(Arrays.asList(origins));
        }
        config.setAllowedMethods(Arrays.asList(
                properties.getCors().getAllowedMethods() != null
                        ? properties.getCors().getAllowedMethods()
                        : new String[]{"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"}));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Location"));
        config.setAllowCredentials(properties.getCors().isAllowCredentials());
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
