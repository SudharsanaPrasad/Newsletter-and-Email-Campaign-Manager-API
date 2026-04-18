package com.example.newsletter.repository;

import com.example.newsletter.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    boolean existsByMailingListIdAndEmailIgnoreCase(Long mailingListId, String email);
}
