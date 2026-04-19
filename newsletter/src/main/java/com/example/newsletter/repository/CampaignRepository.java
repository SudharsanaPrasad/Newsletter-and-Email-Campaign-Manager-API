package com.example.newsletter.repository;

import com.example.newsletter.entity.Campaign;
import com.example.newsletter.entity.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Page<Campaign> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Campaign> findByOwnerIdAndStatus(Long ownerId, CampaignStatus status, Pageable pageable);

    // used by the scheduler to pick up campaigns whose time has arrived
    List<Campaign> findByStatusAndScheduledTimeLessThanEqual(CampaignStatus status, LocalDateTime time);

    // used to stop a mailing list being deleted while campaigns still point at it
    boolean existsByMailingListId(Long mailingListId);
}
