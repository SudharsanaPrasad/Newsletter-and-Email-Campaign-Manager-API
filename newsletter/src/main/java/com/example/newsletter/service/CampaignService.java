package com.example.newsletter.service;

import com.example.newsletter.dto.CampaignRequest;
import com.example.newsletter.dto.CampaignResponse;
import com.example.newsletter.dto.EmailSendLogResponse;
import com.example.newsletter.dto.ScheduleRequest;
import com.example.newsletter.entity.Campaign;
import com.example.newsletter.entity.CampaignStatus;
import com.example.newsletter.entity.MailingList;
import com.example.newsletter.entity.User;
import com.example.newsletter.exception.BusinessRuleException;
import com.example.newsletter.exception.ResourceNotFoundException;
import com.example.newsletter.repository.CampaignRepository;
import com.example.newsletter.repository.EmailSendLogRepository;
import com.example.newsletter.repository.MailingListRepository;
import com.example.newsletter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final MailingListRepository mailingListRepository;
    private final EmailSendLogRepository emailSendLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public CampaignResponse create(String username, CampaignRequest request) {
        User owner = currentUser(username);
        MailingList list = resolveOwnedList(username, request.mailingListId());

        Campaign campaign = new Campaign();
        campaign.setName(request.name());
        campaign.setSubject(request.subject());
        campaign.setContent(request.content());
        campaign.setMailingList(list);
        campaign.setOwner(owner);
        applyStatus(campaign, request.status(), request.scheduledTime());

        return toResponse(campaignRepository.save(campaign));
    }

    @Transactional
    public CampaignResponse update(String username, Long id, CampaignRequest request) {
        Campaign campaign = getOwnedCampaign(username, id);

        // once a campaign has gone out we treat it as final - editing it would be a lie
        if (campaign.getStatus() == CampaignStatus.SENT) {
            throw new BusinessRuleException("Cannot edit a campaign that has already been sent");
        }

        MailingList list = resolveOwnedList(username, request.mailingListId());
        campaign.setName(request.name());
        campaign.setSubject(request.subject());
        campaign.setContent(request.content());
        campaign.setMailingList(list);
        applyStatus(campaign, request.status(), request.scheduledTime());

        return toResponse(campaignRepository.save(campaign));
    }

    @Transactional
    public CampaignResponse schedule(String username, Long id, ScheduleRequest request) {
        Campaign campaign = getOwnedCampaign(username, id);

        if (campaign.getStatus() == CampaignStatus.SENT) {
            throw new BusinessRuleException("Cannot schedule a campaign that has already been sent");
        }

        requireFuture(request.scheduledTime());
        campaign.setStatus(CampaignStatus.SCHEDULED);
        campaign.setScheduledTime(request.scheduledTime());

        return toResponse(campaignRepository.save(campaign));
    }

    @Transactional
    public void delete(String username, Long id) {
        Campaign campaign = getOwnedCampaign(username, id);
        campaignRepository.delete(campaign);
    }

    @Transactional(readOnly = true)
    public Page<CampaignResponse> findMyCampaigns(String username, CampaignStatus status, Pageable pageable) {
        User owner = currentUser(username);
        Page<Campaign> campaigns = (status == null)
                ? campaignRepository.findByOwnerId(owner.getId(), pageable)
                : campaignRepository.findByOwnerIdAndStatus(owner.getId(), status, pageable);
        return campaigns.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CampaignResponse findMyCampaign(String username, Long id) {
        return toResponse(getOwnedCampaign(username, id));
    }

    @Transactional(readOnly = true)
    public List<EmailSendLogResponse> findLogs(String username, Long id) {
        getOwnedCampaign(username, id);
        return emailSendLogRepository.findByCampaignIdOrderBySentAtAsc(id).stream()
                .map(logEntry -> new EmailSendLogResponse(
                        logEntry.getId(),
                        logEntry.getRecipientName(),
                        logEntry.getRecipientEmail(),
                        logEntry.getSentAt(),
                        logEntry.getStatus()))
                .toList();
    }

    // decides the status of a campaign on create/update and keeps scheduledTime in sync
    private void applyStatus(Campaign campaign, CampaignStatus requested, LocalDateTime scheduledTime) {
        CampaignStatus status = (requested == null) ? CampaignStatus.DRAFT : requested;

        if (status == CampaignStatus.SENT) {
            throw new BusinessRuleException(
                    "A campaign cannot be saved as SENT; it is sent automatically at its scheduled time");
        }

        if (status == CampaignStatus.SCHEDULED) {
            if (scheduledTime == null) {
                throw new BusinessRuleException("scheduledTime is required when status is SCHEDULED");
            }
            requireFuture(scheduledTime);
            campaign.setStatus(CampaignStatus.SCHEDULED);
            campaign.setScheduledTime(scheduledTime);
        } else {
            // DRAFT - clear any scheduled time so a draft never looks scheduled
            campaign.setStatus(CampaignStatus.DRAFT);
            campaign.setScheduledTime(null);
        }
    }

    private void requireFuture(LocalDateTime scheduledTime) {
        if (!scheduledTime.isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Scheduled time must be in the future");
        }
    }

    private Campaign getOwnedCampaign(String username, Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", id));
        if (!campaign.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have access to this campaign");
        }
        return campaign;
    }

    private MailingList resolveOwnedList(String username, Long listId) {
        MailingList list = mailingListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Mailing list", listId));
        if (!list.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have access to this mailing list");
        }
        return list;
    }

    private User currentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", null));
    }

    private CampaignResponse toResponse(Campaign campaign) {
        return new CampaignResponse(
                campaign.getId(),
                campaign.getName(),
                campaign.getSubject(),
                campaign.getContent(),
                campaign.getMailingList().getId(),
                campaign.getMailingList().getName(),
                campaign.getStatus(),
                campaign.getScheduledTime(),
                campaign.getSentTime(),
                campaign.getCreatedAt());
    }
}
