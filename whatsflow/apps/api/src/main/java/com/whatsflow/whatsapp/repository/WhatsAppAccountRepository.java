package com.whatsflow.whatsapp.repository;

import com.whatsflow.whatsapp.domain.WhatsAppAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WhatsAppAccountRepository extends JpaRepository<WhatsAppAccount, UUID> {
    Optional<WhatsAppAccount> findByTenantIdAndDeletedFalse(UUID tenantId);
    Optional<WhatsAppAccount> findByPhoneNumberIdAndDeletedFalse(String phoneNumberId);
}
