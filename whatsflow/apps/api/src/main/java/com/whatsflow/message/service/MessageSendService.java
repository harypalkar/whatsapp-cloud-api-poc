package com.whatsflow.message.service;


import com.whatsflow.config.WhatsAppProperties;
import com.whatsflow.conversation.domain.Conversation;
import com.whatsflow.conversation.repository.ConversationRepository;
import com.whatsflow.customer.domain.Customer;
import com.whatsflow.customer.repository.CustomerRepository;
import com.whatsflow.exception.NotFoundException;
import com.whatsflow.message.domain.Message;
import com.whatsflow.message.repository.MessageRepository;
import com.whatsflow.tenant.TenantContext;
import com.whatsflow.whatsapp.spi.WhatsAppProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageSendService {
    private final WhatsAppProvider provider;
    private final WhatsAppProperties props;
    private final CustomerRepository customers;
    private final ConversationRepository conversations;
    private final MessageRepository messages;

    public MessageSendService(WhatsAppProvider provider, WhatsAppProperties props,
                              CustomerRepository customers, ConversationRepository conversations,
                              MessageRepository messages) {
        this.provider = provider; this.props = props; this.customers = customers;
        this.conversations = conversations; this.messages = messages;
    }

    @Transactional
    public Message sendText(UUID customerId, String body) {
        UUID tenantId = TenantContext.requireTenantId();
        Customer customer = customers.findByIdAndTenantIdAndDeletedFalse(customerId, tenantId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        Conversation conv = conversations.findByTenantIdAndCustomerIdAndDeletedFalse(tenantId, customerId)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setTenantId(tenantId);
                    c.setCustomerId(customerId);
                    c.setStatus("OPEN");
                    return conversations.save(c);
                });
        Map<String, Object> meta = provider.sendText(props.getPhoneNumberId(), props.getAccessToken(),
                customer.getMobileE164(), body);
        String wamid = extractId(meta);
        Message msg = new Message();
        msg.setTenantId(tenantId);
        msg.setConversationId(conv.getId());
        msg.setDirection("OUT");
        msg.setType("text");
        msg.setBody(body);
        msg.setWaMessageId(wamid);
        msg.setDeliveryStatus("accepted");
        messages.save(msg);
        conv.setLastMessagePreview(body.length() > 200 ? body.substring(0, 200) : body);
        conversations.save(conv);
        return msg;
    }

    @Transactional
    public Message sendTemplate(UUID customerId, String templateName, String language, List<String> params) {
        UUID tenantId = TenantContext.requireTenantId();
        Customer customer = customers.findByIdAndTenantIdAndDeletedFalse(customerId, tenantId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        Map<String, Object> meta = provider.sendTemplate(props.getPhoneNumberId(), props.getAccessToken(),
                customer.getMobileE164(), templateName, language == null ? "en" : language, params);
        Conversation conv = conversations.findByTenantIdAndCustomerIdAndDeletedFalse(tenantId, customerId)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setTenantId(tenantId); c.setCustomerId(customerId); c.setStatus("OPEN");
                    return conversations.save(c);
                });
        Message msg = new Message();
        msg.setTenantId(tenantId);
        msg.setConversationId(conv.getId());
        msg.setDirection("OUT");
        msg.setType("template");
        msg.setBody(templateName);
        msg.setWaMessageId(extractId(meta));
        msg.setDeliveryStatus("accepted");
        return messages.save(msg);
    }

    @SuppressWarnings("unchecked")
    private String extractId(Map<String, Object> meta) {
        Object messagesObj = meta.get("messages");
        if (messagesObj instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> m) {
            return String.valueOf(m.get("id"));
        }
        return null;
    }
}
