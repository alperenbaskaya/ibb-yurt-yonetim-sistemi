package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.AuditLogPageResponse;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.exception.InvalidAuditHistoryRequestException;
import com.ibb.yurtlar.service.GlobalAuditHistoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/global-history")
@PreAuthorize("hasRole('ADMIN')")
public class GlobalAuditHistoryController {
    private final GlobalAuditHistoryService globalAuditHistoryService;

    public GlobalAuditHistoryController(
            GlobalAuditHistoryService globalAuditHistoryService
    ) {
        this.globalAuditHistoryService = globalAuditHistoryService;
    }

    @GetMapping("/dormitory-operations")
    public AuditLogPageResponse getDormitoryOperations(
            @RequestParam Long dormitoryId,
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        return globalAuditHistoryService.getDormitoryOperations(
                dormitoryId,
                parseDormitoryCategory(category),
                page,
                size,
                authentication.getName()
        );
    }

    @GetMapping("/system-management")
    public AuditLogPageResponse getSystemManagement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        return globalAuditHistoryService.getSystemManagement(
                page,
                size,
                authentication.getName()
        );
    }

    private AuditCategory parseDormitoryCategory(String value) {
        try {
            return AuditCategory.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidAuditHistoryRequestException(
                    "Geçersiz yurt operasyon kategorisi."
            );
        }
    }
}
