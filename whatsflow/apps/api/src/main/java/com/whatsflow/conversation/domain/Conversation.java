package com.whatsflow.conversation.domain;


import com.whatsflow.common.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "conversations")
public class Conversation extends TenantEntity {

    @Column(name = "customer_id", nullable = false) private java.util.UUID customerId;
    @Column(name = "assigned_user_id") private java.util.UUID assignedUserId;
    @Column(nullable = false, length = 32) private String status = "OPEN";
    @Column(name = "unread_count", nullable = false) private int unreadCount = 0;
    @Column(name = "last_message_preview", length = 500) private String lastMessagePreview;
    @Column(name = "last_customer_message_at") private java.time.Instant lastCustomerMessageAt;
    @Column(name = "window_expires_at") private java.time.Instant windowExpiresAt;

}
