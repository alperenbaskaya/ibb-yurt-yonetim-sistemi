package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.AuditLogPageResponse;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.exception.InvalidAuditHistoryRequestException;
import com.ibb.yurtlar.service.DormitoryAdminAuditHistoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin-history")
@PreAuthorize("hasRole('ADMIN')")
public class DormitoryAdminAuditHistoryController {
    private final DormitoryAdminAuditHistoryService historyService;

    public DormitoryAdminAuditHistoryController(
            DormitoryAdminAuditHistoryService historyService
    ) {
        this.historyService = historyService;
    }

    @GetMapping("/me/dormitory")
    public AuditLogPageResponse getOwnDormitoryHistory(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        return historyService.getOwnDormitoryHistory(
                parseCategory(category),
                page,
                size,
                authentication.getName()
        );
    }

    private AuditCategory parseCategory(String value) {
        try {
            return AuditCategory.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidAuditHistoryRequestException(
                    "Geçersiz yurt işlem kategorisi."
            );
        }
    }
}
