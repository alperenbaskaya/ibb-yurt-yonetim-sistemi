package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.AuditLogResponse;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.event.AuditLogRecordedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ReviewerRecentActivityListener {
    private final ReviewerRecentActivityProjection projection;

    public ReviewerRecentActivityListener(ReviewerRecentActivityProjection projection) {
        this.projection = projection;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuditLogRecorded(AuditLogRecordedEvent event) {
        if (event.dormitoryId() == null || !isIncluded(event.action())) {
            return;
        }

        projection.add(new AuditLogResponse(
                event.id(), event.actorUserId(), event.actorName(), event.actorRole(),
                event.subjectStudentId(), event.subjectStudentName(), event.dormitoryId(),
                event.dormitoryName(), event.category(), event.action(), event.entityType(),
                event.entityId(), event.targetLabel(), event.description(), event.createdAt()
        ));
    }

    private boolean isIncluded(AuditAction action) {
        return action == AuditAction.DOCUMENT_UPLOADED
                || action == AuditAction.DOCUMENT_REUPLOADED;
    }
}
