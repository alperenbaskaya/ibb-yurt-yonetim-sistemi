package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.enums.Role;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long actorUserId,
        String actorName,
        Role actorRole,
        Long subjectStudentId,
        String subjectStudentName,
        Long dormitoryId,
        String dormitoryName,
        AuditCategory category,
        AuditAction action,
        AuditEntityType entityType,
        Long entityId,
        String targetLabel,
        String description,
        LocalDateTime createdAt
) {
}
