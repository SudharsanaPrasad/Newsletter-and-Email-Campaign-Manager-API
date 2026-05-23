package com.example.newsletter.service;

import com.example.newsletter.dto.MailingListRequest;
import com.example.newsletter.dto.MailingListResponse;
import com.example.newsletter.dto.SubscriberRequest;
import com.example.newsletter.dto.SubscriberResponse;
import com.example.newsletter.entity.MailingList;
import com.example.newsletter.entity.Subscriber;
import com.example.newsletter.entity.User;
import com.example.newsletter.exception.BusinessRuleException;
import com.example.newsletter.exception.DuplicateResourceException;
import com.example.newsletter.exception.ResourceNotFoundException;
import com.example.newsletter.repository.CampaignRepository;
import com.example.newsletter.repository.MailingListRepository;
import com.example.newsletter.repository.SubscriberRepository;
import com.example.newsletter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MailingListService {

    private final MailingListRepository mailingListRepository;
    private final SubscriberRepository subscriberRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    @Transactional
    public MailingListResponse create(String username, MailingListRequest request) {
        User owner = currentUser(username);

        MailingList list = new MailingList();
        list.setName(request.name());
        list.setOwner(owner);

        return toResponse(mailingListRepository.save(list));
    }

    @Transactional(readOnly = true)
    public List<MailingListResponse> findMyLists(String username) {
        User owner = currentUser(username);
        return mailingListRepository.findByOwnerId(owner.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MailingListResponse findMyList(String username, Long id) {
        return toResponse(getOwnedList(username, id));
    }

    @Transactional
    public MailingListResponse update(String username, Long id, MailingListRequest request) {
        MailingList list = getOwnedList(username, id);
        list.setName(request.name());
        return toResponse(mailingListRepository.save(list));
    }

    @Transactional
    public void delete(String username, Long id) {
        MailingList list = getOwnedList(username, id);

        // a campaign points at exactly one list, so deleting a list out from under
        // a campaign would leave it dangling - block it with a clear message instead
        if (campaignRepository.existsByMailingListId(id)) {
            throw new BusinessRuleException(
                    "Cannot delete a mailing list that campaigns are linked to");
        }

        mailingListRepository.delete(list);
    }

    @Transactional
    public SubscriberResponse addSubscriber(String username, Long listId, SubscriberRequest request) {
        MailingList list = getOwnedList(username, listId);

        // a subscriber email must be unique within one list (spec: no duplicate emails)
        if (subscriberRepository.existsByMailingListIdAndEmailIgnoreCase(listId, request.email())) {
            throw new DuplicateResourceException(
                    "Email is already subscribed to this list: " + request.email());
        }

        Subscriber subscriber = new Subscriber();
        subscriber.setName(request.name());
        subscriber.setEmail(request.email());
        subscriber.setMailingList(list);

        // save the subscriber directly so the generated id is set on the way back
        return toSubscriberResponse(subscriberRepository.save(subscriber));
    }

    @Transactional
    public void removeSubscriber(String username, Long listId, Long subscriberId) {
        MailingList list = getOwnedList(username, listId);

        Subscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscriber", subscriberId));

        // make sure the subscriber actually belongs to this list, not another one
        if (!subscriber.getMailingList().getId().equals(list.getId())) {
            throw new ResourceNotFoundException("Subscriber", subscriberId);
        }

        subscriberRepository.delete(subscriber);
    }

    // loads a list and checks the caller owns it - used by every read and write
    private MailingList getOwnedList(String username, Long id) {
        MailingList list = mailingListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mailing list", id));
        if (!list.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have access to this mailing list");
        }
        return list;
    }

    private User currentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", null));
    }

    private MailingListResponse toResponse(MailingList list) {
        List<SubscriberResponse> subscribers = list.getSubscribers().stream()
                .map(this::toSubscriberResponse)
                .toList();
        return new MailingListResponse(
                list.getId(),
                list.getName(),
                list.getCreatedAt(),
                subscribers.size(),
                subscribers);
    }

    private SubscriberResponse toSubscriberResponse(Subscriber subscriber) {
        return new SubscriberResponse(
                subscriber.getId(),
                subscriber.getName(),
                subscriber.getEmail());
    }
}
