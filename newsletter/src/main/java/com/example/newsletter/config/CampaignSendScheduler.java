package com.example.newsletter.config;

import com.example.newsletter.service.CampaignSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// checks once a minute for scheduled campaigns that are due and sends them.
// this is what turns a SCHEDULED campaign into a SENT one at its chosen time.
@Component
@RequiredArgsConstructor
public class CampaignSendScheduler {

    private final CampaignSenderService campaignSenderService;

    @Scheduled(fixedDelay = 60000)
    public void sendDueCampaigns() {
        campaignSenderService.dispatchDueCampaigns();
    }
}
