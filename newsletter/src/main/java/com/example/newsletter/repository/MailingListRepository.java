package com.example.newsletter.repository;

import com.example.newsletter.entity.MailingList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MailingListRepository extends JpaRepository<MailingList, Long> {

    List<MailingList> findByOwnerId(Long ownerId);
}
