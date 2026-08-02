package com.whatsflow.scheduler;


import com.whatsflow.campaign.domain.Campaign;
import com.whatsflow.campaign.repository.CampaignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Component
public class CampaignScheduler {
    private static final Logger log = LoggerFactory.getLogger(CampaignScheduler.class);
    private final CampaignRepository campaigns;
    public CampaignScheduler(CampaignRepository campaigns) { this.campaigns = campaigns; }

    @Scheduled(fixedDelayString = "60000")
    @Transactional
    public void promoteDueCampaigns() {
        List<Campaign> due = campaigns.findByStatusAndDeletedFalse("SCHEDULED");
        Instant now = Instant.now();
        for (Campaign c : due) {
            if (c.getScheduledAt() != null && !c.getScheduledAt().isAfter(now)) {
                c.setStatus("RUNNING");
                campaigns.save(c);
                log.info("Campaign {} moved to RUNNING", c.getId());
            }
        }
    }
}
