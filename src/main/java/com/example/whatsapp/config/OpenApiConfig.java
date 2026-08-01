package com.example.whatsapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI whatsappOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("WhatsApp Cloud API POC")
                        .description("""
                                REST API for WhatsApp Cloud integration.

                                **Endpoints**
                                - `POST /api/send` — Send outbound WhatsApp text messages
                                - `GET/POST /webhook/whatsapp` — Meta webhook (ngrok callback)
                                - `GET/POST /api/v1/webhooks/meta/whatsapp` — Spec alias
                                - `GET /health` — Plain-text health
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Altitude Labs")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")))
                .tags(List.of(
                        new Tag()
                                .name("WhatsApp")
                                .description("Send messages via WhatsApp Cloud API"),
                        new Tag()
                                .name("Meta WhatsApp Webhook")
                                .description("Meta webhook verification and inbound messages"),
                        new Tag()
                                .name("Health")
                                .description("Application health check")));
    }
}
