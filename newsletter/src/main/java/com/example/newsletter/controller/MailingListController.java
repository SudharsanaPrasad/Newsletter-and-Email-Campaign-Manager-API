package com.example.newsletter.controller;

import com.example.newsletter.dto.MailingListRequest;
import com.example.newsletter.dto.MailingListResponse;
import com.example.newsletter.dto.SubscriberRequest;
import com.example.newsletter.dto.SubscriberResponse;
import com.example.newsletter.service.MailingListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mailing-lists")
@RequiredArgsConstructor
public class MailingListController {

    private final MailingListService mailingListService;

    @PostMapping
    public ResponseEntity<MailingListResponse> create(Authentication authentication,
                                                       @Valid @RequestBody MailingListRequest request) {
        MailingListResponse response = mailingListService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<MailingListResponse> myLists(Authentication authentication) {
        return mailingListService.findMyLists(authentication.getName());
    }

    @GetMapping("/{id}")
    public MailingListResponse myList(Authentication authentication, @PathVariable Long id) {
        return mailingListService.findMyList(authentication.getName(), id);
    }

    @PutMapping("/{id}")
    public MailingListResponse update(Authentication authentication, @PathVariable Long id,
                                      @Valid @RequestBody MailingListRequest request) {
        return mailingListService.update(authentication.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        mailingListService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/subscribers")
    public ResponseEntity<SubscriberResponse> addSubscriber(Authentication authentication,
                                                            @PathVariable Long id,
                                                            @Valid @RequestBody SubscriberRequest request) {
        SubscriberResponse response =
                mailingListService.addSubscriber(authentication.getName(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}/subscribers/{subscriberId}")
    public ResponseEntity<Void> removeSubscriber(Authentication authentication,
                                                 @PathVariable Long id,
                                                 @PathVariable Long subscriberId) {
        mailingListService.removeSubscriber(authentication.getName(), id, subscriberId);
        return ResponseEntity.noContent().build();
    }
}
