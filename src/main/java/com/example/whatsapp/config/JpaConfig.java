package com.example.whatsapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = {
        "com.example.whatsapp.repository",
        "com.example.whatsapp.webhook.repository"
})
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
}
