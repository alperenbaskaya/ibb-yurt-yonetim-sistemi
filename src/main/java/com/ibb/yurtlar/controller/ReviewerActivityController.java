package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.AuditLogResponse;
import com.ibb.yurtlar.service.ReviewerActivityService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviewer-activity")
@PreAuthorize("hasRole('REVIEWER')")
public class ReviewerActivityController {
    private final ReviewerActivityService reviewerActivityService;

    public ReviewerActivityController(ReviewerActivityService reviewerActivityService) {
        this.reviewerActivityService = reviewerActivityService;
    }

    @GetMapping("/me/recent")
    public List<AuditLogResponse> getMyRecentActivity(Authentication authentication) {
        return reviewerActivityService.getRecentActivity(authentication.getName());
    }
}
