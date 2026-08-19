package com.ibb.yurtlar.controller;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

import com.ibb.yurtlar.dto.AuditLogPageResponse;
import com.ibb.yurtlar.dto.AuditLogAnalyticsResponse;
import com.ibb.yurtlar.dto.AuditLogReindexResponse;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.search.audit.AuditLogSearchCriteria;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.service.GlobalAuditHistoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

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

    @GetMapping("/search")
    public AuditLogPageResponse search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) AuditCategory category,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) Role actorRole,
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) Long subjectStudentId,
            @RequestParam(required = false) Long dormitoryId,
            @RequestParam(required = false) AuditEntityType entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        return globalAuditHistoryService.search(new AuditLogSearchCriteria(q, category, action,
                actorRole, actorUserId, subjectStudentId, dormitoryId, entityType, entityId,
                from, to, page, size), authentication.getName());
    }

    @GetMapping("/analytics")
    public AuditLogAnalyticsResponse analytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Authentication authentication
    ) {
        return globalAuditHistoryService.analytics(from, to, authentication.getName());
    }

    @PostMapping("/elasticsearch/reindex")
    public AuditLogReindexResponse reindex(Authentication authentication) {
        return globalAuditHistoryService.reindex(authentication.getName());
    }

    private AuditCategory parseDormitoryCategory(String value) {
        try {
            return AuditCategory.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(INVALID_AUDIT_HISTORY_REQUEST,
                    "Geçersiz yurt operasyon kategorisi."
            );
        }
    }
}
