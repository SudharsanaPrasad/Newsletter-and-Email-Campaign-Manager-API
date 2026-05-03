package com.example.newsletter.service;

import com.example.newsletter.entity.Campaign;
import com.example.newsletter.entity.CampaignStatus;
import com.example.newsletter.entity.EmailSendLog;
import com.example.newsletter.entity.Subscriber;
import com.example.newsletter.repository.CampaignRepository;
import com.example.newsletter.repository.EmailSendLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// does the actual "sending" for campaigns whose scheduled time has passed.
// called on a timer by CampaignSendScheduler, but kept separate so the logic
// can also be triggered and tested on its own.
@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignSenderService {

    private final CampaignRepository campaignRepository;
    private final EmailSendLogRepository emailSendLogRepository;
    private final MockEmailService mockEmailService;

    @Transactional
    public int dispatchDueCampaigns() {
        LocalDateTime now = LocalDateTime.now();
        List<Campaign> due = campaignRepository
                .findByStatusAndScheduledTimeLessThanEqual(CampaignStatus.SCHEDULED, now);

        for (Campaign campaign : due) {
            sendCampaign(campaign, now);
        }
        return due.size();
    }

    private void sendCampaign(Campaign campaign, LocalDateTime sentAt) {
        List<Subscriber> subscribers = campaign.getMailingList().getSubscribers();

        for (Subscriber subscriber : subscribers) {
            mockEmailService.send(subscriber.getEmail(), subscriber.getName(),
                    campaign.getSubject(), campaign.getContent());

            EmailSendLog logEntry = new EmailSendLog();
            logEntry.setCampaign(campaign);
            logEntry.setRecipientName(subscriber.getName());
            logEntry.setRecipientEmail(subscriber.getEmail());
            logEntry.setSentAt(sentAt);
            logEntry.setStatus("SENT");
            emailSendLogRepository.save(logEntry);
        }

        campaign.setStatus(CampaignStatus.SENT);
        campaign.setSentTime(sentAt);
        campaignRepository.save(campaign);

        log.info("Campaign '{}' (id {}) sent to {} subscriber(s)",
                campaign.getName(), campaign.getId(), subscribers.size());
    }
}
