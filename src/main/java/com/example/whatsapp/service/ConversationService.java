package com.example.whatsapp.service;

import com.example.whatsapp.entity.Conversation;
import com.example.whatsapp.entity.MessageDirection;
import com.example.whatsapp.entity.MessageRecord;
import com.example.whatsapp.repository.ConversationRepository;
import com.example.whatsapp.repository.MessageRecordRepository;
import com.example.whatsapp.tenant.TenantContext;
import com.example.whatsapp.tenant.TenantFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private static final Logger databaseLog = LoggerFactory.getLogger("com.example.whatsapp.database");

    private final ConversationRepository conversationRepository;
    private final MessageRecordRepository messageRecordRepository;

    @Transactional
    public Conversation saveInbound(String mobile, String message, LocalDateTime createdAt) {
        return save(mobile, message, MessageDirection.IN, createdAt, null, "received", "text", currentCompanyId());
    }

    @Transactional
    public Conversation saveOutbound(String mobile, String message) {
        return saveOutbound(mobile, message, null, "text", currentCompanyId());
    }

    @Transactional
    public Conversation saveOutbound(String mobile, String message, String waMessageId) {
        return saveOutbound(mobile, message, waMessageId, "text", currentCompanyId());
    }

    @Transactional
    public Conversation saveOutbound(
            String mobile, String message, String waMessageId, String messageType, Long companyId) {
        return save(
                mobile,
                message,
                MessageDirection.OUT,
                LocalDateTime.now(ZoneOffset.UTC),
                waMessageId,
                "accepted",
                messageType,
                companyId != null ? companyId : currentCompanyId());
    }

    @Transactional
    public void updateDeliveryStatus(String waMessageId, String status) {
        messageRecordRepository.findByWaMessageId(waMessageId).ifPresent(record -> {
            String normalized = status == null ? null : status.toLowerCase(Locale.ROOT);
            record.setDeliveryStatus(normalized);
            if ("read".equals(normalized)) {
                record.setReadStatus("read");
            }
            messageRecordRepository.save(record);
            databaseLog.info(
                    "Database operation: UPDATE messages waMessageId={}, deliveryStatus={}, readStatus={}",
                    waMessageId,
                    record.getDeliveryStatus(),
                    record.getReadStatus());
        });
    }

    @Transactional(readOnly = true)
    public List<MessageRecord> timeline(String mobile) {
        return messageRecordRepository.findByCustomerMobileOrderByCreatedAtAsc(mobile);
    }

    private Conversation save(
            String mobile,
            String message,
            MessageDirection direction,
            LocalDateTime createdAt,
            String waMessageId,
            String deliveryStatus,
            String messageType,
            Long companyId) {
        Conversation conversation = Conversation.builder()
                .mobile(mobile)
                .message(message)
                .direction(direction)
                .createdAt(createdAt)
                .build();

        Conversation saved = conversationRepository.save(conversation);

        MessageRecord record = MessageRecord.builder()
                .companyId(companyId)
                .customerMobile(mobile)
                .direction(direction.name())
                .messageType(messageType != null ? messageType : "text")
                .body(message)
                .waMessageId(waMessageId)
                .deliveryStatus(deliveryStatus)
                .createdAt(createdAt)
                .build();
        messageRecordRepository.save(record);

        databaseLog.info(
                "Database operation: INSERT conversations/messages id={}, companyId={}, mobile={}, direction={}, createdAt={}",
                saved.getId(),
                companyId,
                saved.getMobile(),
                saved.getDirection(),
                saved.getCreatedAt());

        return saved;
    }

    private static Long currentCompanyId() {
        Long id = TenantContext.getCompanyId();
        return id != null ? id : TenantFilter.DEFAULT_COMPANY_ID;
    }
}
