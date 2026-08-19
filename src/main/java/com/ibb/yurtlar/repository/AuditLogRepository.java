package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.AuditLog;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.AuditEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @Query("SELECT COALESCE(MAX(audit.id), 0) FROM AuditLog audit")
    Long findHighWaterMark();

    @Query("""
        SELECT audit
        FROM AuditLog audit
        WHERE audit.id > :lastId
          AND audit.id <= :highWaterMark
        ORDER BY audit.id ASC
        """)
    List<AuditLog> findReindexBatch(
            @Param("lastId") Long lastId,
            @Param("highWaterMark") Long highWaterMark,
            Pageable pageable
    );

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

    @Query("""
        SELECT audit
        FROM AuditLog audit
        WHERE audit.dormitoryId = :dormitoryId
          AND audit.category = :category
          AND audit.action IN :actions
        ORDER BY audit.createdAt DESC, audit.id DESC
        """)
    List<AuditLog> findRecentByDormitoryCategoryAndActions(
            @Param("dormitoryId") Long dormitoryId,
            @Param("category") AuditCategory category,
            @Param("actions") Collection<AuditAction> actions,
            Pageable pageable
    );

    List<AuditLog> findByEntityTypeAndEntityIdAndActionInOrderByCreatedAtAscIdAsc(
            AuditEntityType entityType,
            Long entityId,
            Collection<AuditAction> actions
    );

    List<AuditLog> findByEntityTypeAndEntityIdInAndActionInOrderByCreatedAtAscIdAsc(
            AuditEntityType entityType,
            Collection<Long> entityIds,
            Collection<AuditAction> actions
    );
}
