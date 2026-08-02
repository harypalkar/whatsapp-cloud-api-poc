package com.whatsflow.config;


import com.whatsflow.tenant.TenantContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;
import java.util.UUID;

@Configuration
public class JpaAuditingConfig {
    @Bean
    AuditorAware<UUID> auditorAware() {
        return () -> TenantContext.getUserId();
    }
}
