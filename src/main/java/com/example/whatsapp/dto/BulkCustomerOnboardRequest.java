package com.example.whatsapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Bulk onboard customers and send Altitude Labs promo")
public class BulkCustomerOnboardRequest {

    @NotEmpty
    @Valid
    private List<CustomerOnboardRequest> customers;
}
