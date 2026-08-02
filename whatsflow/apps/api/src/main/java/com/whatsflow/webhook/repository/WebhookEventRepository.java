package com.whatsflow.webhook.repository;


import com.whatsflow.webhook.domain.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {}
