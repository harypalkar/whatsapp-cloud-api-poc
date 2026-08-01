package com.example.whatsapp.service;

import com.example.whatsapp.config.WhatsAppProperties;
import com.example.whatsapp.dto.BulkCustomerOnboardRequest;
import com.example.whatsapp.dto.CustomerOnboardRequest;
import com.example.whatsapp.dto.SendTemplateRequest;
import com.example.whatsapp.dto.WhatsAppRequest;
import com.example.whatsapp.entity.Customer;
import com.example.whatsapp.exception.BusinessException;
import com.example.whatsapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerOnboardingService {

    public static final String SHOP_URL = "https://www.altitudelabs.in/";
    public static final long ALTITUDE_COMPANY_ID = 1L;

    private final CustomerRepository customerRepository;
    private final WhatsAppService whatsAppService;
    private final WhatsAppProperties whatsAppProperties;

    @Transactional
    public Map<String, Object> onboard(CustomerOnboardRequest request) {
        String mobile = normalizeMobile(request.getMobile());
        String name = StringUtils.hasText(request.getName()) ? request.getName().trim() : "Customer";
        String promo = StringUtils.hasText(request.getPromoCode()) ? request.getPromoCode() : "WELCOME100";

        Customer customer = customerRepository.findByMobile(mobile)
                .map(existing -> {
                    existing.setName(name);
                    existing.setOptedIn(true);
                    if (existing.getCompanyId() == null) {
                        existing.setCompanyId(ALTITUDE_COMPANY_ID);
                    }
                    return customerRepository.save(existing);
                })
                .orElseGet(() -> customerRepository.save(Customer.builder()
                        .companyId(ALTITUDE_COMPANY_ID)
                        .mobile(mobile)
                        .name(name)
                        .optedIn(true)
                        .build()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("customerId", customer.getId());
        result.put("mobile", customer.getMobile());
        result.put("name", customer.getName());
        result.put("optedIn", customer.getOptedIn());
        result.put("shopUrl", SHOP_URL);
        result.put("promoCode", promo);
        result.put("status", "ONBOARDED");

        boolean sendWelcome = request.getSendWelcome() == null || request.getSendWelcome();
        if (sendWelcome) {
            // Cold outreach default: ALWAYS marketing template.
            // Free text / CTA / buttons require the customer to message first (24h window) — that is why
            // Bharat only received messages after saying Hi. Direct promo must use messageStyle=template.
            String requested = request.getMessageStyle() == null
                    ? "template"
                    : request.getMessageStyle().toLowerCase(Locale.ROOT);
            boolean allowSession = Boolean.TRUE.equals(request.getAllowSessionMessage());
            String style = "template";
            if (allowSession && ("cta".equals(requested) || "text".equals(requested) || "buttons".equals(requested))) {
                style = requested;
            } else if (!"template".equals(requested) && !allowSession) {
                result.put(
                        "forcedTemplate",
                        true);
                result.put(
                        "reason",
                        "Direct promo to any number requires an approved marketing template. "
                                + "Set allowSessionMessage=true only when customer already said Hi (24h window).");
            }
            result.put("messageStyle", style);
            try {
                Map<String, Object> meta = sendWelcomeByStyle(style, mobile, name, promo, request);
                result.put("welcomeSent", true);
                result.put("whatsapp", meta);
            } catch (Exception ex) {
                log.error("Welcome WhatsApp failed for mobile={}: {}", mobile, ex.getMessage());
                result.put("welcomeSent", false);
                result.put("welcomeError", ex.getMessage());
                result.put(
                        "hint",
                        "Create/approve marketing template altitude_welcome_promo (language en) in Meta WhatsApp Manager, "
                                + "then call POST /api/v1/messages/send-template. "
                                + "Check GET /api/v1/meta/templates for the exact approved name/language.");
            }
        } else {
            result.put("welcomeSent", false);
        }

        return result;
    }

    private Map<String, Object> sendWelcomeByStyle(
            String style,
            String mobile,
            String name,
            String promo,
            CustomerOnboardRequest request) {
        String body = buildWelcomeMessage(name, promo);
        return switch (style) {
            case "buttons" -> whatsAppService.sendInteractiveButtons(mobile, body);
            case "text" -> whatsAppService.sendMessage(WhatsAppRequest.builder()
                    .mobile(mobile)
                    .message(body)
                    .build());
            case "cta" -> whatsAppService.sendShopCta(mobile, body, "Shop Now", SHOP_URL);
            default -> whatsAppService.sendPromoTemplate(SendTemplateRequest.builder()
                    .mobile(mobile)
                    .customerName(name)
                    .promoCode(promo)
                    .build());
        };
    }

    public List<Map<String, Object>> onboardBulk(BulkCustomerOnboardRequest request) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (CustomerOnboardRequest customer : request.getCustomers()) {
            results.add(onboard(customer));
        }
        return results;
    }

    public List<Customer> listCustomers() {
        return customerRepository.findAll();
    }

    public static String buildWelcomeMessage(String name, String promo) {
        return """
                Hello %s

                Welcome to Altitude Labs™

                Pure Himalayan Shilajit — lab-tested, altitude-harvested.

                ✅ Boost testosterone support
                ✅ Strength & stamina
                ✅ Faster recovery
                ✅ Natural energy

                Use Promo Code: %s
                Get ₹100 OFF on your first order.

                Shop now: %s

                Reply INTERESTED to talk to a specialist
                Reply STOP to opt out
                """.formatted(name, promo, SHOP_URL).trim();
    }

    private String normalizeMobile(String mobile) {
        String digits = mobile == null ? "" : mobile.replaceAll("[^0-9]", "");
        if (digits.length() == 10) {
            digits = "91" + digits;
        }
        if (digits.length() < 11) {
            throw new BusinessException("Invalid mobile: " + mobile, HttpStatus.BAD_REQUEST);
        }
        return digits;
    }
}
