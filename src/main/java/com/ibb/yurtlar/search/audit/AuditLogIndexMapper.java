package com.ibb.yurtlar.search.audit;

import com.ibb.yurtlar.entity.AuditLog;
import com.ibb.yurtlar.kafka.event.AuditLogRecordedKafkaEvent;
import org.springframework.stereotype.Component;

@Component
public class AuditLogIndexMapper {

    public AuditLogSearchDocument toDocument(AuditLog auditLog) {
        return new AuditLogSearchDocument(
                auditLog.getId(), auditLog.getActorUserId(), auditLog.getActorName(),
                auditLog.getActorRole(), auditLog.getSubjectStudentId(),
                auditLog.getSubjectStudentName(), auditLog.getDormitoryId(),
                auditLog.getDormitoryName(), auditLog.getCategory(), auditLog.getAction(),
                auditLog.getEntityType(), auditLog.getEntityId(), auditLog.getTargetLabel(),
                auditLog.getDescription(), auditLog.getCreatedAt(), 1
        );
    }

    public AuditLogSearchDocument toDocument(
            AuditLogRecordedKafkaEvent event
    ) {
        return new AuditLogSearchDocument(
                event.auditLogId(),
                event.actorUserId(),
                event.actorName(),
                event.actorRole(),
                event.subjectStudentId(),
                event.subjectStudentName(),
                event.dormitoryId(),
                event.dormitoryName(),
                event.category(),
                event.action(),
                event.entityType(),
                event.entityId(),
                event.targetLabel(),
                event.description(),
                event.createdAt(),
                event.schemaVersion()
        );
    }
}
