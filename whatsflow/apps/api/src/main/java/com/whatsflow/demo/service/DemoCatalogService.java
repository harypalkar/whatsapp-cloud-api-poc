package com.whatsflow.demo.service;

import com.whatsflow.campaign.repository.CampaignRepository;
import com.whatsflow.company.service.CompanyService;
import com.whatsflow.conversation.repository.ConversationRepository;
import com.whatsflow.customer.repository.CustomerRepository;
import com.whatsflow.forms.repository.FormRepository;
import com.whatsflow.message.repository.MessageRepository;
import com.whatsflow.tenant.TenantContext;
import com.whatsflow.whatsapp.repository.WhatsAppAccountRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class DemoCatalogService {

    private final CustomerRepository customers;
    private final CampaignRepository campaigns;
    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final FormRepository forms;
    private final WhatsAppAccountRepository whatsAppAccounts;
    private final CompanyService companyService;

    public DemoCatalogService(CustomerRepository customers, CampaignRepository campaigns,
                              ConversationRepository conversations, MessageRepository messages,
                              FormRepository forms, WhatsAppAccountRepository whatsAppAccounts,
                              CompanyService companyService) {
        this.customers = customers;
        this.campaigns = campaigns;
        this.conversations = conversations;
        this.messages = messages;
        this.forms = forms;
        this.whatsAppAccounts = whatsAppAccounts;
        this.companyService = companyService;
    }

    public Map<String, Object> fullCatalog() {
        UUID tenantId = TenantContext.requireTenantId();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("company", companyService.getCurrent());
        out.put("whatsapp", whatsapp(tenantId));
        out.put("dashboard", dashboard(tenantId));
        out.put("templates", templates());
        out.put("media", media());
        out.put("automations", automations());
        out.put("analytics", analytics(tenantId));
        out.put("reports", reports(tenantId));
        out.put("billing", billing());
        out.put("admin", admin());
        out.put("ai", aiTools());
        out.put("notifications", notifications());
        out.put("scenarios", scenarios());
        out.put("settings", settings());
        out.put("forms", forms.findByTenantIdAndDeletedFalse(tenantId));
        return out;
    }

    public Map<String, Object> dashboard(UUID tenantId) {
        long customerCount = customers.countByTenantIdAndDeletedFalse(tenantId);
        long campaignCount = campaigns.findByTenantIdAndDeletedFalse(tenantId, Pageable.unpaged()).getTotalElements();
        long conversationCount = conversations.countByTenantIdAndDeletedFalse(tenantId);
        long unread = conversations.sumUnreadByTenantId(tenantId);
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("messagesToday", 1284);
        d.put("customers", customerCount);
        d.put("campaigns", campaignCount);
        d.put("conversations", conversationCount);
        d.put("unreadChats", unread);
        d.put("revenueMonth", 249990);
        d.put("currency", "INR");
        d.put("deliveryRate", 97.4);
        d.put("readRate", 68.2);
        d.put("chartMessages", List.of(820, 910, 1005, 980, 1120, 1210, 1284));
        d.put("chartLabels", List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"));
        d.put("recentActivity", List.of(
                Map.of("title", "Health Camp campaign completed", "time", "12 min ago"),
                Map.of("title", "42 unread chats assigned to Dr Rajesh", "time", "28 min ago"),
                Map.of("title", "Admission Open template approved", "time", "1 hr ago"),
                Map.of("title", "GST invoice INV-2041 generated", "time", "2 hr ago"),
                Map.of("title", "Insurance Renewal workflow executed", "time", "3 hr ago")
        ));
        d.put("quickActions", List.of("New campaign", "Open inbox", "Import customers", "Run demo scenario"));
        return d;
    }

    private Map<String, Object> whatsapp(UUID tenantId) {
        return whatsAppAccounts.findByTenantIdAndDeletedFalse(tenantId)
                .map(a -> Map.<String, Object>of(
                        "status", a.getStatus(),
                        "displayPhone", Objects.toString(a.getDisplayPhone(), ""),
                        "phoneNumberId", a.getPhoneNumberId(),
                        "wabaId", Objects.toString(a.getWabaId(), ""),
                        "businessId", Objects.toString(a.getBusinessId(), ""),
                        "verifiedName", Objects.toString(a.getVerifiedName(), ""),
                        "webhookVerifyToken", Objects.toString(a.getWebhookVerifyToken(), ""),
                        "accessTokenMasked", "••••••••demo-token",
                        "connected", "CONNECTED".equalsIgnoreCase(a.getStatus())
                ))
                .orElse(Map.of("status", "DISCONNECTED", "connected", false));
    }

    private List<Map<String, Object>> templates() {
        String[] names = {
                "health_camp_invite", "admission_open", "festival_sale", "property_expo", "restaurant_offer",
                "insurance_renewal", "appointment_reminder", "report_ready", "feedback_request", "payment_due",
                "welcome_patient", "opd_followup", "vaccination_drive", "corporate_wellness", "diwali_greetings"
        };
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            list.add(Map.of(
                    "id", "tpl-" + (i + 1),
                    "name", names[i],
                    "language", "en",
                    "category", i % 2 == 0 ? "MARKETING" : "UTILITY",
                    "status", "APPROVED",
                    "body", "Hello {{1}}, " + names[i].replace('_', ' ') + " — open WhatsApp for details.",
                    "buttons", List.of("Book Now", "Call Hospital"),
                    "coupon", "WF" + (200 + i)
            ));
        }
        return list;
    }

    private List<Map<String, Object>> media() {
        return List.of(
                Map.of("id", "m1", "name", "Health Camp Banner.jpg", "type", "image", "folder", "Campaigns", "size", "1.2 MB"),
                Map.of("id", "m2", "name", "Admission Brochure.pdf", "type", "pdf", "folder", "Brochures", "size", "840 KB"),
                Map.of("id", "m3", "name", "Hospital Tour.mp4", "type", "video", "folder", "Videos", "size", "12.4 MB"),
                Map.of("id", "m4", "name", "Patient Import.xlsx", "type", "excel", "folder", "Imports", "size", "220 KB"),
                Map.of("id", "m5", "name", "Consent Form.docx", "type", "word", "folder", "Forms", "size", "96 KB"),
                Map.of("id", "m6", "name", "Festival Offer.png", "type", "image", "folder", "Campaigns", "size", "640 KB"),
                Map.of("id", "m7", "name", "Property Expo Flyer.pdf", "type", "pdf", "folder", "Brochures", "size", "1.1 MB"),
                Map.of("id", "m8", "name", "Menu Weekend.pdf", "type", "pdf", "folder", "Restaurant", "size", "480 KB")
        );
    }

    private List<Map<String, Object>> automations() {
        return List.of(
                workflow("Hospital Appointment Reminder", List.of("Start", "Delay 24h", "WhatsApp Template", "Condition Replied?", "End")),
                workflow("School Admission Campaign", List.of("Start", "WhatsApp", "Form Capture", "Email", "End")),
                workflow("Restaurant Offer", List.of("Start", "Segment Filter", "WhatsApp Coupon", "SMS Fallback", "End")),
                workflow("Property Inquiry", List.of("Start", "Webhook Lead", "Assign Agent", "WhatsApp", "End")),
                workflow("Insurance Renewal", List.of("Start", "Condition Due Date", "WhatsApp", "API CRM Update", "End"))
        );
    }

    private Map<String, Object> workflow(String name, List<String> nodes) {
        return Map.of("id", name.toLowerCase(Locale.ROOT).replace(' ', '-'), "name", name, "status", "ACTIVE", "nodes", nodes, "runs", 120 + nodes.size() * 11);
    }

    private Map<String, Object> analytics(UUID tenantId) {
        long customerTotal = this.customers.countByTenantIdAndDeletedFalse(tenantId);
        return Map.of(
                "messages", List.of(12, 14, 15, 18, 21, 24, 28, 31, 29, 33, 36, 40),
                "revenue", List.of(80, 92, 101, 110, 125, 140, 155, 162, 170, 188, 210, 249),
                "customers", List.of(40, 55, 70, 95, 120, 160, 210, 260, 310, 370, 430, customerTotal),
                "campaigns", List.of(1, 2, 2, 3, 4, 5, 6, 8, 10, 12, 15, 20),
                "months", List.of("Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug"),
                "growthPct", 18.6
        );
    }

    private Map<String, Object> reports(UUID tenantId) {
        return Map.of(
                "campaignReport", Map.of("sent", 48210, "delivered", 46900, "read", 32100, "replied", 4120),
                "revenueReport", Map.of("mrr", 14999, "invoicesPaid", 8, "pending", 1, "currency", "INR"),
                "customerReport", Map.of("total", customers.countByTenantIdAndDeletedFalse(tenantId), "optedIn", 470, "blacklisted", 5),
                "conversationReport", Map.of("open", 112, "closed", 38, "avgFirstResponseMin", 4.2)
        );
    }

    private Map<String, Object> billing() {
        return Map.of(
                "plan", "PROFESSIONAL",
                "plans", List.of(
                        Map.of("code", "STARTER", "price", 999, "agents", 3),
                        Map.of("code", "GROWTH", "price", 4999, "agents", 10),
                        Map.of("code", "PROFESSIONAL", "price", 14999, "agents", 50),
                        Map.of("code", "ENTERPRISE", "price", 49999, "agents", 500)
                ),
                "invoices", List.of(
                        Map.of("id", "INV-2041", "amount", 14999, "gst", 2699.82, "status", "PAID", "date", LocalDate.now().minusDays(12).toString()),
                        Map.of("id", "INV-2033", "amount", 14999, "gst", 2699.82, "status", "PAID", "date", LocalDate.now().minusDays(42).toString())
                ),
                "coupons", List.of(Map.of("code", "WELCOME20", "discount", "20%"), Map.of("code", "HOSPITAL10", "discount", "10%")),
                "payments", List.of(Map.of("mode", "UPI", "status", "SUCCESS"), Map.of("mode", "NETBANKING", "status", "SUCCESS"))
        );
    }

    private Map<String, Object> admin() {
        return Map.of(
                "companies", 128,
                "subscriptions", Map.of("starter", 40, "growth", 52, "professional", 28, "enterprise", 8),
                "revenue", 1864200,
                "serverHealth", "HEALTHY",
                "usage", Map.of("messages", "2.1M", "storage", "48 GB", "apiCalls", "9.4M"),
                "tickets", List.of(
                        Map.of("id", "T-901", "subject", "Template approval delay", "status", "OPEN"),
                        Map.of("id", "T-882", "subject", "Billing GST update", "status", "RESOLVED")
                )
        );
    }

    private Map<String, Object> aiTools() {
        return Map.of(
                "suggestions", List.of(
                        "Namaste! Your ABC Hospital appointment is confirmed for tomorrow 10:30 AM.",
                        "Thanks for your interest in our Health Camp. Reply YES to reserve a slot.",
                        "We can share the admission brochure PDF right away. Shall I send it?"
                ),
                "summary", "Patient asked for appointment, shared insurance details, and confirmed Saturday OPD slot.",
                "faq", List.of(
                        Map.of("q", "OPD timings?", "a", "Mon–Sat 9 AM to 6 PM"),
                        Map.of("q", "Emergency?", "a", "24x7 emergency desk on +91 95126 18333")
                ),
                "sentiment", "positive",
                "translation", Map.of("hi", "नमस्ते, आपका अपॉइंटमेंट कन्फर्म है।"),
                "campaignIdeas", List.of("Monsoon Immunity Camp", "Senior Citizen Health Package", "Corporate Wellness Drive")
        );
    }

    private List<Map<String, Object>> notifications() {
        return List.of(
                Map.of("id", "n1", "title", "12 chats unread", "type", "inbox", "read", false),
                Map.of("id", "n2", "title", "Campaign Health Camp finished", "type", "campaign", "read", false),
                Map.of("id", "n3", "title", "Invoice INV-2041 paid", "type", "billing", "read", true),
                Map.of("id", "n4", "title", "Reminder: Insurance Renewal scenario", "type", "reminder", "read", false)
        );
    }

    private List<Map<String, Object>> scenarios() {
        return List.of(
                Map.of("id", "s1", "name", "Hospital Appointment Reminder", "sector", "Hospital", "status", "READY"),
                Map.of("id", "s2", "name", "School Admission Campaign", "sector", "School", "status", "READY"),
                Map.of("id", "s3", "name", "Restaurant Offer", "sector", "Restaurant", "status", "READY"),
                Map.of("id", "s4", "name", "Property Inquiry", "sector", "Real Estate", "status", "READY"),
                Map.of("id", "s5", "name", "Insurance Renewal", "sector", "Insurance", "status", "READY")
        );
    }

    private Map<String, Object> settings() {
        return Map.of(
                "theme", "light",
                "language", "en-IN",
                "timezone", "Asia/Kolkata",
                "branding", Map.of("primary", "#0e4a38", "accent", "#c9952a", "logo", "ABC Hospital")
        );
    }
}
