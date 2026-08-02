package com.whatsflow.conversation.service;


import com.whatsflow.conversation.domain.Conversation;
import com.whatsflow.conversation.repository.ConversationRepository;
import com.whatsflow.exception.NotFoundException;
import com.whatsflow.message.domain.Message;
import com.whatsflow.message.repository.MessageRepository;
import com.whatsflow.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class ConversationService {
    private final ConversationRepository conversations;
    private final MessageRepository messages;

    public ConversationService(ConversationRepository conversations, MessageRepository messages) {
        this.conversations = conversations; this.messages = messages;
    }

    @Transactional(readOnly = true)
    public Page<Conversation> list(Pageable pageable) {
        return conversations.findByTenantIdAndDeletedFalseOrderByModifiedDateDesc(TenantContext.requireTenantId(), pageable);
    }

    @Transactional
    public Conversation assign(UUID id, UUID agentId) {
        Conversation c = require(id);
        c.setAssignedUserId(agentId);
        c.setStatus("ASSIGNED");
        return conversations.save(c);
    }

    @Transactional(readOnly = true)
    public Page<Message> timeline(UUID conversationId, Pageable pageable) {
        require(conversationId);
        return messages.findByTenantIdAndConversationIdAndDeletedFalseOrderByCreatedDateAsc(
                TenantContext.requireTenantId(), conversationId, pageable);
    }

    private Conversation require(UUID id) {
        return conversations.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.requireTenantId())
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
    }
}
