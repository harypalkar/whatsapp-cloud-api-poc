package com.whatsflow.customer.domain;


import com.whatsflow.common.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "customers")
public class Customer extends TenantEntity {

    @Column(name = "mobile_e164", nullable = false, length = 32) private String mobileE164;
    private String name;
    private String email;
    @Column(name = "opted_in", nullable = false) private boolean optedIn = true;
    @Column(nullable = false) private boolean blacklisted = false;
    @Column(name = "attributes_json") private String attributesJson;

}
