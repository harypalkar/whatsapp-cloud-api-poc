package com.example.whatsapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "customer_mobile", nullable = false, length = 20)
    private String customerMobile;

    @Column(nullable = false, length = 10)
    private String direction;

    @Column(name = "message_type", nullable = false, length = 50)
    private String messageType;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "wa_message_id", length = 256)
    private String waMessageId;

    @Column(name = "delivery_status", length = 50)
    private String deliveryStatus;

    @Column(name = "read_status", length = 50)
    private String readStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
