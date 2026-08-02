package com.whatsflow.identity.repository;


import com.whatsflow.identity.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByTenantIdAndEmailIgnoreCaseAndDeletedFalse(UUID tenantId, String email);
    Optional<UserAccount> findByEmailIgnoreCaseAndDeletedFalse(String email);



}
