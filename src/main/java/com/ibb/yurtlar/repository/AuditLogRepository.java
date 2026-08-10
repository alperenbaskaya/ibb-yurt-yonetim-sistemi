package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.AuditLog;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditEntityType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    boolean existsByActionAndEntityTypeAndEntityId(
            AuditAction action,
            AuditEntityType entityType,
            Long entityId
    );
}
