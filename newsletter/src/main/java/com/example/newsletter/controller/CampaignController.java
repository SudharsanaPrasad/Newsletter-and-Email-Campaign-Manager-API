package com.example.newsletter.controller;

import com.example.newsletter.dto.CampaignRequest;
import com.example.newsletter.dto.CampaignResponse;
import com.example.newsletter.dto.EmailSendLogResponse;
import com.example.newsletter.dto.ScheduleRequest;
import com.example.newsletter.entity.CampaignStatus;
import com.example.newsletter.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    public ResponseEntity<CampaignResponse> create(Authentication authentication,
                                                   @Valid @RequestBody CampaignRequest request) {
        CampaignResponse response = campaignService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public Page<CampaignResponse> myCampaigns(
            Authentication authentication,
            @RequestParam(required = false) CampaignStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return campaignService.findMyCampaigns(authentication.getName(), status, pageable);
    }

    @GetMapping("/{id}")
    public CampaignResponse myCampaign(Authentication authentication, @PathVariable Long id) {
        return campaignService.findMyCampaign(authentication.getName(), id);
    }

    @PutMapping("/{id}")
    public CampaignResponse update(Authentication authentication, @PathVariable Long id,
                                   @Valid @RequestBody CampaignRequest request) {
        return campaignService.update(authentication.getName(), id, request);
    }

    @PostMapping("/{id}/schedule")
    public CampaignResponse schedule(Authentication authentication, @PathVariable Long id,
                                     @Valid @RequestBody ScheduleRequest request) {
        return campaignService.schedule(authentication.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        campaignService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/logs")
    public List<EmailSendLogResponse> logs(Authentication authentication, @PathVariable Long id) {
        return campaignService.findLogs(authentication.getName(), id);
    }
}
