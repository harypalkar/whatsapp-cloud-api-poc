package com.whatsflow.conversation.repository;


import com.whatsflow.conversation.domain.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Page<Conversation> findByTenantIdAndDeletedFalseOrderByModifiedDateDesc(UUID tenantId, Pageable pageable);
    Optional<Conversation> findByIdAndTenantIdAndDeletedFalse(UUID id, UUID tenantId);
    Optional<Conversation> findByTenantIdAndCustomerIdAndDeletedFalse(UUID tenantId, UUID customerId);
}
