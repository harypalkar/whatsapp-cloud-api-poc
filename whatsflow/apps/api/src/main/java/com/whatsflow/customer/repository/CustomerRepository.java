package com.whatsflow.customer.repository;


import com.whatsflow.customer.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Page<Customer> findByTenantIdAndDeletedFalse(UUID tenantId, Pageable pageable);
    Page<Customer> findByTenantIdAndDeletedFalseAndNameContainingIgnoreCaseOrTenantIdAndDeletedFalseAndMobileE164Containing(
            UUID t1, String name, UUID t2, String mobile, Pageable pageable);
    Optional<Customer> findByIdAndTenantIdAndDeletedFalse(UUID id, UUID tenantId);
    Optional<Customer> findByTenantIdAndMobileE164AndDeletedFalse(UUID tenantId, String mobile);
    long countByTenantIdAndDeletedFalse(UUID tenantId);
}
