package com.ibb.yurtlar.controller;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

import com.ibb.yurtlar.dto.AuditLogPageResponse;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.search.audit.AuditLogSearchCriteria;
import com.ibb.yurtlar.service.DormitoryAdminAuditHistoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

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

    @GetMapping("/me/dormitory/search")
    public AuditLogPageResponse searchOwnDormitory(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) AuditCategory category,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) Role actorRole,
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) Long subjectStudentId,
            @RequestParam(required = false) AuditEntityType entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        return historyService.searchOwnDormitory(new AuditLogSearchCriteria(q, category, action,
                actorRole, actorUserId, subjectStudentId, null, entityType, entityId,
                from, to, page, size), authentication.getName());
    }

    private AuditCategory parseCategory(String value) {
        try {
            return AuditCategory.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(INVALID_AUDIT_HISTORY_REQUEST,
                    "Geçersiz yurt işlem kategorisi."
            );
        }
    }
}
