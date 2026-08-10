package com.ibb.yurtlar.entity;

import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_dormitory_created", columnList = "dormitory_id, created_at"),
        @Index(name = "idx_audit_category_created", columnList = "category, created_at"),
        @Index(name = "idx_audit_actor", columnList = "actor_user_id"),
        @Index(name = "idx_audit_subject_student", columnList = "subject_student_id")
})
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;
    @Column(name = "actor_name", nullable = false, length = 200)
    private String actorName;
    @Enumerated(EnumType.STRING)
    @Column(name = "actor_role", nullable = false, length = 30)
    private Role actorRole;

    @Column(name = "subject_student_id")
    private Long subjectStudentId;
    @Column(name = "subject_student_name", length = 200)
    private String subjectStudentName;
    @Column(name = "dormitory_id")
    private Long dormitoryId;
    @Column(name = "dormitory_name", length = 200)
    private String dormitoryName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AuditCategory category;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 40)
    private AuditEntityType entityType;
    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    @Column(name = "target_label", nullable = false, length = 250)
    private String targetLabel;
    @Column(nullable = false, length = 1000)
    private String description;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AuditLog(Long actorUserId, String actorName, Role actorRole,
                    Long subjectStudentId, String subjectStudentName,
                    Long dormitoryId, String dormitoryName,
                    AuditCategory category, AuditAction action,
                    AuditEntityType entityType, Long entityId,
                    String targetLabel, String description) {
        this.actorUserId = actorUserId;
        this.actorName = actorName;
        this.actorRole = actorRole;
        this.subjectStudentId = subjectStudentId;
        this.subjectStudentName = subjectStudentName;
        this.dormitoryId = dormitoryId;
        this.dormitoryName = dormitoryName;
        this.category = category;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.targetLabel = targetLabel;
        this.description = description;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
