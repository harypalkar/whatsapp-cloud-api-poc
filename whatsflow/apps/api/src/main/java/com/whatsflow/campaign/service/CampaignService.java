package com.whatsflow.campaign.service;


import com.whatsflow.campaign.domain.Campaign;
import com.whatsflow.campaign.dto.CampaignRequest;
import com.whatsflow.campaign.repository.CampaignRepository;
import com.whatsflow.exception.NotFoundException;
import com.whatsflow.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
public class CampaignService {
    private final CampaignRepository repo;
    public CampaignService(CampaignRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public Page<Campaign> list(Pageable pageable) {
        return repo.findByTenantIdAndDeletedFalse(TenantContext.requireTenantId(), pageable);
    }

    @Transactional
    public Campaign create(CampaignRequest req) {
        Campaign c = new Campaign();
        c.setTenantId(TenantContext.requireTenantId());
        c.setName(req.name());
        c.setTemplateName(req.templateName());
        c.setLanguage(req.language() == null ? "en" : req.language());
        c.setPromoCode(req.promoCode());
        c.setStatus("DRAFT");
        return repo.save(c);
    }

    @Transactional
    public Campaign schedule(UUID id, Instant when) {
        Campaign c = require(id);
        c.setScheduledAt(when);
        c.setStatus("SCHEDULED");
        return repo.save(c);
    }

    @Transactional
    public Campaign transition(UUID id, String status) {
        Campaign c = require(id);
        c.setStatus(status);
        return repo.save(c);
    }

    private Campaign require(UUID id) {
        return repo.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.requireTenantId())
                .orElseThrow(() -> new NotFoundException("Campaign not found"));
    }
}
