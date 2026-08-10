package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.AuditLog;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.AuditEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    boolean existsByActionAndEntityTypeAndEntityId(
            AuditAction action,
            AuditEntityType entityType,
            Long entityId
    );

    Page<AuditLog> findByDormitoryIdAndCategoryOrderByCreatedAtDescIdDesc(
            Long dormitoryId,
            AuditCategory category,
            Pageable pageable
    );

    Page<AuditLog> findByCategoryOrderByCreatedAtDescIdDesc(
            AuditCategory category,
            Pageable pageable
    );
}
