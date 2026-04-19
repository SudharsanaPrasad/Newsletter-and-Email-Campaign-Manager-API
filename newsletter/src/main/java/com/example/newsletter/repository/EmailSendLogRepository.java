package com.example.newsletter.repository;

import com.example.newsletter.entity.EmailSendLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailSendLogRepository extends JpaRepository<EmailSendLog, Long> {

    List<EmailSendLog> findByCampaignIdOrderBySentAtAsc(Long campaignId);
}
