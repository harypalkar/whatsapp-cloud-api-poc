package com.example.whatsapp.webhook.repository;

import com.example.whatsapp.webhook.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
}
