package com.example.whatsapp.repository;

import com.example.whatsapp.entity.Conversation;
import com.example.whatsapp.entity.MessageDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByMobileOrderByCreatedAtDesc(String mobile);

    List<Conversation> findByMobileAndDirectionOrderByCreatedAtDesc(
            String mobile,
            MessageDirection direction);
}
