package com.example.whatsapp.repository;

import com.example.whatsapp.entity.CompanyWhatsAppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyWhatsAppConfigRepository extends JpaRepository<CompanyWhatsAppConfig, Long> {

    Optional<CompanyWhatsAppConfig> findByCompanyIdAndActiveTrue(Long companyId);
}
