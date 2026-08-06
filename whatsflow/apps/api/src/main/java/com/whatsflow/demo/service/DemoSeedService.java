package com.whatsflow.demo.service;

import com.whatsflow.campaign.domain.Campaign;
import com.whatsflow.campaign.repository.CampaignRepository;
import com.whatsflow.common.crypto.AesGcmEncryptor;
import com.whatsflow.company.domain.Company;
import com.whatsflow.company.repository.CompanyRepository;
import com.whatsflow.conversation.domain.Conversation;
import com.whatsflow.conversation.repository.ConversationRepository;
import com.whatsflow.customer.domain.Customer;
import com.whatsflow.customer.repository.CustomerRepository;
import com.whatsflow.demo.config.DemoProperties;
import com.whatsflow.forms.domain.FormDefinition;
import com.whatsflow.forms.repository.FormRepository;
import com.whatsflow.identity.domain.Role;
import com.whatsflow.identity.domain.UserAccount;
import com.whatsflow.identity.repository.RoleRepository;
import com.whatsflow.identity.repository.UserAccountRepository;
import com.whatsflow.message.domain.Message;
import com.whatsflow.message.repository.MessageRepository;
import com.whatsflow.whatsapp.domain.WhatsAppAccount;
import com.whatsflow.whatsapp.repository.WhatsAppAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@Order(100)
public class DemoSeedService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoSeedService.class);

    private static final String[] FIRST = {
            "Aarav", "Vihaan", "Aditya", "Arjun", "Reyansh", "Sai", "Krishna", "Ishaan", "Kabir", "Atharv",
            "Ananya", "Aadhya", "Diya", "Myra", "Anika", "Sara", "Ira", "Pari", "Riya", "Navya",
            "Rohan", "Karan", "Nikhil", "Rahul", "Amit", "Priya", "Neha", "Pooja", "Sneha", "Kavya"
    };
    private static final String[] LAST = {
            "Sharma", "Patel", "Singh", "Kumar", "Gupta", "Reddy", "Nair", "Iyer", "Joshi", "Mehta",
            "Shah", "Desai", "Chopra", "Malhotra", "Banerjee", "Chatterjee", "Pillai", "Kulkarni", "Verma", "Rao"
    };
    private static final String[] CITIES = {
            "Mumbai", "Pune", "Bengaluru", "Hyderabad", "Chennai", "Delhi", "Ahmedabad", "Jaipur", "Kochi", "Indore"
    };
    private static final String[] STATES = {
            "Maharashtra", "Maharashtra", "Karnataka", "Telangana", "Tamil Nadu", "Delhi", "Gujarat", "Rajasthan", "Kerala", "Madhya Pradesh"
    };
    private static final String[] TAGS = {
            "VIP", "OPD", "IPD", "Insurance", "Follow-up", "New Lead", "Corporate", "Pediatric", "Dental", "Cardiology"
    };
    private static final String[] CAMPAIGN_NAMES = {
            "Health Camp", "Admission Open", "Festival Sale", "Property Expo", "Restaurant Offer",
            "Insurance Renewal", "School Open Day", "Dental Checkup Week", "Travel Flash Deal", "Finance EMI Offer",
            "Wellness Sunday", "Blood Donation Drive", "Ayurveda Package", "Eye Camp", "Maternity Package",
            "Diwali Greetings", "New Year Health Plan", "Diabetes Awareness", "Vaccination Reminder", "Corporate Wellness"
    };
    private static final String[] FORM_NAMES = {
            "Lead Form", "Admission Form", "Complaint Form", "Registration Form", "Feedback Form",
            "Contact Form", "Appointment Request", "Insurance Claim Prefill", "OPD Feedback", "Corporate Enquiry"
    };
    private static final String[] CHAT_SNIPPETS = {
            "Namaste, I need an appointment tomorrow.",
            "Can you share the health camp schedule?",
            "Please send the admission brochure PDF.",
            "Is this coupon still valid?",
            "Thank you, doctor. Reminder received.",
            "I want to renew my insurance policy.",
            "Property visit for Sunday 11 AM?",
            "Table booking for 4 people tonight.",
            "School admission form link please.",
            "My report is ready - please confirm."
    };

    private final DemoProperties demo;
    private final CompanyRepository companies;
    private final UserAccountRepository users;
    private final RoleRepository roles;
    private final CustomerRepository customers;
    private final CampaignRepository campaigns;
    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final FormRepository forms;
    private final WhatsAppAccountRepository whatsAppAccounts;
    private final PasswordEncoder encoder;
    private final AesGcmEncryptor encryptor;

    public DemoSeedService(DemoProperties demo, CompanyRepository companies, UserAccountRepository users,
                           RoleRepository roles, CustomerRepository customers, CampaignRepository campaigns,
                           ConversationRepository conversations, MessageRepository messages, FormRepository forms,
                           WhatsAppAccountRepository whatsAppAccounts, PasswordEncoder encoder,
                           AesGcmEncryptor encryptor) {
        this.demo = demo;
        this.companies = companies;
        this.users = users;
        this.roles = roles;
        this.customers = customers;
        this.campaigns = campaigns;
        this.conversations = conversations;
        this.messages = messages;
        this.forms = forms;
        this.whatsAppAccounts = whatsAppAccounts;
        this.encoder = encoder;
        this.encryptor = encryptor;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!demo.isEnabled()) {
            return;
        }
        if (users.findByEmailIgnoreCaseAndDeletedFalse(demo.getEmail()).isPresent()) {
            log.info("Demo workspace already seeded for {}", demo.getEmail());
            return;
        }
        log.info("Seeding WhatsFlow demo workspace: {} / {}", demo.getCompanyName(), demo.getEmail());

        Company company = new Company();
        company.setName(demo.getCompanyName());
        company.setSlug("abc-hospital-demo");
        company.setStatus("ACTIVE");
        company.setTimezone("Asia/Kolkata");
        company.setGstin("27AABCU9603R1ZM");
        company.setAddressLine1("12 Wellness Avenue");
        company.setCity("Mumbai");
        company.setState("Maharashtra");
        company.setPincode("400001");
        company.setPlanCode("PROFESSIONAL");
        company.setOnboardingStep(5);
        company.setOnboardingCompleted(true);
        company.setWhatsappConnected(true);
        companies.save(company);
        UUID tenantId = company.getId();

        Role admin = new Role();
        admin.setTenantId(tenantId);
        admin.setCode("COMPANY_ADMIN");
        admin.setName("Company Admin");
        roles.save(admin);

        UserAccount user = new UserAccount();
        user.setTenantId(tenantId);
        user.setEmail(demo.getEmail().toLowerCase(Locale.ROOT));
        user.setPasswordHash(encoder.encode(demo.getPassword()));
        user.setFullName(demo.getFullName());
        user.setEmailVerified(true);
        user.setEnabled(true);
        users.save(user);

        WhatsAppAccount wa = new WhatsAppAccount();
        wa.setTenantId(tenantId);
        wa.setPhoneNumberId("1226308087231072");
        wa.setWabaId("1583394760167591");
        wa.setBusinessId("demo-business-901");
        wa.setDisplayPhone("+91 95126 18333");
        wa.setVerifiedName("ABC Hospital");
        wa.setAccessTokenEnc(encryptor.encrypt("demo-access-token"));
        wa.setWebhookVerifyToken("whatsflow-demo-verify");
        wa.setStatus("CONNECTED");
        whatsAppAccounts.save(wa);

        List<Customer> customerBatch = new ArrayList<>();
        Random rnd = new Random(42);
        for (int i = 1; i <= demo.getCustomers(); i++) {
            Customer c = new Customer();
            c.setTenantId(tenantId);
            String first = FIRST[rnd.nextInt(FIRST.length)];
            String last = LAST[rnd.nextInt(LAST.length)];
            c.setName(first + " " + last);
            c.setEmail((first + "." + last + i + "@demo.whatsflow.ai").toLowerCase(Locale.ROOT));
            c.setMobileE164("+9198" + String.format("%08d", 10000000 + i));
            c.setOptedIn(i % 17 != 0);
            c.setBlacklisted(i % 97 == 0);
            int cityIdx = rnd.nextInt(CITIES.length);
            String tag = TAGS[rnd.nextInt(TAGS.length)];
            c.setAttributesJson("{\"city\":\"" + CITIES[cityIdx] + "\",\"state\":\"" + STATES[cityIdx]
                    + "\",\"tag\":\"" + tag + "\",\"group\":\"" + (i % 2 == 0 ? "Patients" : "Leads")
                    + "\",\"purchases\":" + (1 + rnd.nextInt(6)) + "}");
            customerBatch.add(c);
            if (customerBatch.size() == 50) {
                customers.saveAll(customerBatch);
                customerBatch.clear();
            }
        }
        if (!customerBatch.isEmpty()) {
            customers.saveAll(customerBatch);
        }

        List<Customer> seededCustomers = customers.findByTenantIdAndDeletedFalse(
                tenantId, org.springframework.data.domain.PageRequest.of(0, demo.getConversations())).getContent();

        for (int i = 0; i < demo.getCampaigns(); i++) {
            Campaign camp = new Campaign();
            camp.setTenantId(tenantId);
            camp.setName(CAMPAIGN_NAMES[i % CAMPAIGN_NAMES.length] + (i >= CAMPAIGN_NAMES.length ? " #" + (i + 1) : ""));
            camp.setStatus(i % 5 == 0 ? "DRAFT" : (i % 5 == 1 ? "SCHEDULED" : "COMPLETED"));
            camp.setTemplateName("altitude_welcome_promo");
            camp.setLanguage("en");
            camp.setPromoCode("WF" + (100 + i));
            camp.setWhatsappAccountId(wa.getId());
            camp.setScheduledAt(Instant.now().plus(i, ChronoUnit.DAYS));
            campaigns.save(camp);
        }

        for (int i = 0; i < Math.min(demo.getConversations(), seededCustomers.size()); i++) {
            Customer c = seededCustomers.get(i);
            Conversation conv = new Conversation();
            conv.setTenantId(tenantId);
            conv.setCustomerId(c.getId());
            conv.setAssignedUserId(user.getId());
            conv.setStatus(i % 4 == 0 ? "CLOSED" : "OPEN");
            conv.setUnreadCount(i % 3);
            String preview = CHAT_SNIPPETS[i % CHAT_SNIPPETS.length];
            conv.setLastMessagePreview(preview);
            conv.setLastCustomerMessageAt(Instant.now().minus(i % 48, ChronoUnit.HOURS));
            conv.setWindowExpiresAt(Instant.now().plus(20, ChronoUnit.HOURS));
            conversations.save(conv);

            Message inbound = new Message();
            inbound.setTenantId(tenantId);
            inbound.setConversationId(conv.getId());
            inbound.setDirection("IN");
            inbound.setType("text");
            inbound.setBody(preview);
            inbound.setDeliveryStatus("READ");
            inbound.setWaMessageId("wamid.demo.in." + i);
            messages.save(inbound);

            Message outbound = new Message();
            outbound.setTenantId(tenantId);
            outbound.setConversationId(conv.getId());
            outbound.setDirection("OUT");
            outbound.setType("text");
            outbound.setBody("Thanks " + c.getName().split(" ")[0] + ", ABC Hospital team will assist you shortly.");
            outbound.setDeliveryStatus(i % 2 == 0 ? "READ" : "DELIVERED");
            outbound.setWaMessageId("wamid.demo.out." + i);
            messages.save(outbound);
        }

        for (int i = 0; i < demo.getForms(); i++) {
            FormDefinition form = new FormDefinition();
            form.setTenantId(tenantId);
            form.setName(FORM_NAMES[i % FORM_NAMES.length]);
            form.setFormType(FORM_NAMES[i % FORM_NAMES.length].toUpperCase(Locale.ROOT).replace(' ', '_'));
            form.setPublicToken("demo-form-" + (i + 1));
            form.setStatus("PUBLISHED");
            form.setSchemaJson("{\"fields\":[\"name\",\"mobile\",\"city\",\"notes\"],\"responses\":" + (20 + i * 7) + "}");
            forms.save(form);
        }

        log.info("Demo seed complete: tenant={}, customers={}, campaigns={}, conversations={}, forms={}",
                tenantId, demo.getCustomers(), demo.getCampaigns(), demo.getConversations(), demo.getForms());
    }
}
