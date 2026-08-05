package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.ReviewerDashboardResponse;
import com.ibb.yurtlar.service.ReviewerDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviewer-dashboard")
public class ReviewerDashboardController {

    private final ReviewerDashboardService
            reviewerDashboardService;

    public ReviewerDashboardController(
            ReviewerDashboardService reviewerDashboardService
    ) {
        this.reviewerDashboardService =
                reviewerDashboardService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('REVIEWER')")
    public ReviewerDashboardResponse getMyDashboard(
            Authentication authentication
    ) {
        return reviewerDashboardService
                .getMyDashboard(
                        authentication.getName()
                );
    }
}