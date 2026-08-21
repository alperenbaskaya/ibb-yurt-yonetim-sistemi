package com.ibb.yurtlar.search.audit;

import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.enums.Role;

import java.time.LocalDateTime;

public record AuditLogSearchCriteria(
        String q,
        AuditCategory category,
        AuditAction action,
        Role actorRole,
        Long actorUserId,
        Long subjectStudentId,
        Long dormitoryId,
        AuditEntityType entityType,
        Long entityId,
        LocalDateTime from,
        LocalDateTime to,
        int page,
        int size
) {
}
