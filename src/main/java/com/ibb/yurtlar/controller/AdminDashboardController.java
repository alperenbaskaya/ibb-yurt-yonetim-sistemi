package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.AdminDashboardResponse;
import com.ibb.yurtlar.dto.DormitoryAdminDashboardResponse;
import com.ibb.yurtlar.dto.GlobalAdminDashboardResponse;
import com.ibb.yurtlar.service.AdminDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin-dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService
            adminDashboardService;

    public AdminDashboardController(
            AdminDashboardService adminDashboardService
    ) {
        this.adminDashboardService =
                adminDashboardService;
    }

    /*
     * Eski ortak dashboard endpoint'i.
     *
     * Şimdilik geriye dönük uyumluluk için kalıyor.
     * React tarafında yeni global ve dormitory endpoint'lerini
     * kullanacağız.
     */
    @GetMapping("/me")
    public AdminDashboardResponse getMyDashboard(
            Authentication authentication
    ) {
        return adminDashboardService
                .getMyDashboard(
                        authentication.getName()
                );
    }

    /*
     * Yalnızca DORMITORY scope admin kullanır.
     */
    @GetMapping("/me/dormitory")
    public DormitoryAdminDashboardResponse
    getMyDormitoryDashboard(
            Authentication authentication
    ) {
        return adminDashboardService
                .getMyDormitoryDashboard(
                        authentication.getName()
                );
    }

    /*
     * Yalnızca GLOBAL scope admin kullanır.
     */
    @GetMapping("/me/global")
    public GlobalAdminDashboardResponse
    getMyGlobalDashboard(
            Authentication authentication
    ) {
        return adminDashboardService
                .getMyGlobalDashboard(
                        authentication.getName()
                );
    }
}