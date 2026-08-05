package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.ReviewerDashboardResponse;
import com.ibb.yurtlar.service.ReviewerDashboardService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@PreAuthorize("hasRole('REVIEWER')")
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

    @GetMapping("/{reviewerId}")
    public ReviewerDashboardResponse getDashboard(
            @PathVariable Long reviewerId
    ) {
        return reviewerDashboardService
                .getDashboard(reviewerId);
    }
}