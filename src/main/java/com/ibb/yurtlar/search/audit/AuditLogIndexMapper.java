package com.ibb.yurtlar.search.audit;

import com.ibb.yurtlar.kafka.event.AuditLogRecordedKafkaEvent;
import org.springframework.stereotype.Component;

@Component
public class AuditLogIndexMapper {

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
