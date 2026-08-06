package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.AdminDashboardResponse;
import com.ibb.yurtlar.service.AdminDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ibb.yurtlar.dto.DormitoryAdminDashboardResponse;

@RestController
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

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardResponse getMyDashboard(
            Authentication authentication
    ) {
        return adminDashboardService
                .getMyDashboard(
                        authentication.getName()
                );
    }

    @GetMapping("/me/dormitory")
    @PreAuthorize("hasRole('ADMIN')")
    public DormitoryAdminDashboardResponse
    getMyDormitoryDashboard(
            Authentication authentication
    ) {
        return adminDashboardService
                .getMyDormitoryDashboard(
                        authentication.getName()
                );
    }
}