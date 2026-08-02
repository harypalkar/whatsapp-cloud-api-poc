package com.whatsflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
@EnableScheduling
@EnableJpaAuditing
public class WhatsFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhatsFlowApplication.class, args);
    }
}
