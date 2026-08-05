package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.AdminDashboardResponse;
import com.ibb.yurtlar.service.AdminDashboardService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin-dashboard")
public class AdminDashboardController {

    private final AdminDashboardService
            adminDashboardService;

    public AdminDashboardController(
            AdminDashboardService adminDashboardService
    ) {
        this.adminDashboardService =
                adminDashboardService;
    }

    @GetMapping("/{adminUserId}")
    public AdminDashboardResponse getDashboard(
            @PathVariable Long adminUserId
    ) {
        return adminDashboardService
                .getDashboard(adminUserId);
    }
}