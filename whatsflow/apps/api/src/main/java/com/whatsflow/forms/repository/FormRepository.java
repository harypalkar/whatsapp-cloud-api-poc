package com.whatsflow.forms.repository;


import com.whatsflow.forms.domain.FormDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface FormRepository extends JpaRepository<FormDefinition, UUID> {
    Optional<FormDefinition> findByPublicTokenAndDeletedFalse(String token);
    java.util.List<FormDefinition> findByTenantIdAndDeletedFalse(UUID tenantId);
}
