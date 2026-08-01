package com.example.whatsapp.repository;

import com.example.whatsapp.entity.MessageRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRecordRepository extends JpaRepository<MessageRecord, Long> {

    Optional<MessageRecord> findByWaMessageId(String waMessageId);

    List<MessageRecord> findByCustomerMobileOrderByCreatedAtAsc(String customerMobile);
}
