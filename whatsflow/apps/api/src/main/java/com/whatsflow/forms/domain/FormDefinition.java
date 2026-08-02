package com.whatsflow.forms.domain;


import com.whatsflow.common.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "forms")
public class FormDefinition extends TenantEntity {
    @Column(nullable = false) private String name;
    @Column(name = "form_type") private String formType;
    @Column(name = "public_token", nullable = false, unique = true) private String publicToken;
    @Column(nullable = false) private String status = "DRAFT";
    @Column(name = "schema_json", columnDefinition = "TEXT") private String schemaJson;
}
