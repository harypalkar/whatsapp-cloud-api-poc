package com.example.whatsapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bulk cold promo via approved marketing template (no customer Hi required)")
public class BulkSendTemplateRequest {

    @Schema(example = "WELCOME100", description = "Default promo when a recipient omits promoCode")
    private String promoCode;

    @Schema(example = "Customer", description = "Default name when a recipient omits customerName")
    private String defaultCustomerName;

    /** Full recipient objects (preferred). */
    @Valid
    private List<SendTemplateRequest> recipients;

    /** Shortcut: only mobiles — uses defaultCustomerName + promoCode. */
    @Schema(example = "[\"917718986249\",\"917506426501\",\"917718884343\"]")
    private List<String> mobiles;

    @AssertTrue(message = "Provide recipients[] or mobiles[]")
    public boolean isRecipientsOrMobilesPresent() {
        boolean hasRecipients = recipients != null && !recipients.isEmpty();
        boolean hasMobiles = mobiles != null && !mobiles.isEmpty();
        return hasRecipients || hasMobiles;
    }

    public List<SendTemplateRequest> resolvedRecipients() {
        List<SendTemplateRequest> out = new ArrayList<>();
        if (recipients != null) {
            for (SendTemplateRequest r : recipients) {
                if (r.getPromoCode() == null || r.getPromoCode().isBlank()) {
                    r.setPromoCode(promoCode != null ? promoCode : "WELCOME100");
                }
                if (r.getCustomerName() == null || r.getCustomerName().isBlank()) {
                    r.setCustomerName(defaultCustomerName != null ? defaultCustomerName : "Customer");
                }
                out.add(r);
            }
        }
        if (mobiles != null) {
            String name = defaultCustomerName != null ? defaultCustomerName : "Customer";
            String promo = promoCode != null ? promoCode : "WELCOME100";
            for (String mobile : mobiles) {
                out.add(SendTemplateRequest.builder()
                        .mobile(mobile)
                        .customerName(name)
                        .promoCode(promo)
                        .build());
            }
        }
        return out;
    }
}
