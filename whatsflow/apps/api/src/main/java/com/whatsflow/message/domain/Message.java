package com.whatsflow.message.domain;


import com.whatsflow.common.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "messages")
public class Message extends TenantEntity {

    @Column(name = "conversation_id", nullable = false) private java.util.UUID conversationId;
    @Column(nullable = false, length = 16) private String direction;
    @Column(nullable = false, length = 32) private String type = "text";
    @Column(columnDefinition = "TEXT") private String body;
    @Column(name = "wa_message_id") private String waMessageId;
    @Column(name = "delivery_status") private String deliveryStatus;
    @Column(name = "meta_errors_json") private String metaErrorsJson;
    @Column(name = "media_url") private String mediaUrl;

}
