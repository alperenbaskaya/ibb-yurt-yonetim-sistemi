package com.ibb.yurtlar.search.audit;

import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.enums.Role;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Dynamic;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.WriteTypeHint;

import java.time.LocalDateTime;

@Getter
@Document(
        indexName = "#{@environment.getProperty('app.elasticsearch.audit-index')}",
        createIndex = true,
        alwaysWriteMapping = true,
        dynamic = Dynamic.STRICT,
        writeTypeHint = WriteTypeHint.FALSE,
        storeIdInSource = false
)
@Mapping(mappingPath = "elasticsearch/audit-log-mapping.json")
public class AuditLogSearchDocument {

    @Id
    private final String id;
    private final Long auditLogId;
    private final Long actorUserId;
    private final String actorName;
    private final Role actorRole;
    private final Long subjectStudentId;
    private final String subjectStudentName;
    private final Long dormitoryId;
    private final String dormitoryName;
    private final AuditCategory category;
    private final AuditAction action;
    private final AuditEntityType entityType;
    private final Long entityId;
    private final String targetLabel;
    private final String description;
    private final LocalDateTime createdAt;
    private final int schemaVersion;

    public AuditLogSearchDocument(
            Long auditLogId,
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
            LocalDateTime createdAt,
            int schemaVersion
    ) {
        this.id = auditLogId.toString();
        this.auditLogId = auditLogId;
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
        this.createdAt = createdAt;
        this.schemaVersion = schemaVersion;
    }
}
