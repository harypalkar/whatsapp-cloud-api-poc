package com.whatsflow.campaign.repository;


import com.whatsflow.campaign.domain.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignRepository extends JpaRepository<Campaign, UUID> {
    Page<Campaign> findByTenantIdAndDeletedFalse(UUID tenantId, Pageable pageable);
    Optional<Campaign> findByIdAndTenantIdAndDeletedFalse(UUID id, UUID tenantId);
    List<Campaign> findByStatusAndDeletedFalse(String status);
}
