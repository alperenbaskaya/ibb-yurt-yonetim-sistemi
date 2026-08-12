package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.AuditLogResponse;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.event.AuditLogRecordedEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ReviewerRecentActivityListenerTest {
    private final ReviewerRecentActivityProjection projection =
            mock(ReviewerRecentActivityProjection.class);
    private final ReviewerRecentActivityListener listener =
            new ReviewerRecentActivityListener(projection);

    @Test
    void documentUploadedUpdatesProjection() {
        listener.onAuditLogRecorded(event(AuditAction.DOCUMENT_UPLOADED, 10L));
        verify(projection).add(any(AuditLogResponse.class));
    }

    @Test
    void documentReuploadedUpdatesProjection() {
        listener.onAuditLogRecorded(event(AuditAction.DOCUMENT_REUPLOADED, 10L));
        verify(projection).add(any(AuditLogResponse.class));
    }

    @Test
    void unrelatedActionDoesNotUpdateProjection() {
        listener.onAuditLogRecorded(event(AuditAction.DOCUMENT_APPROVED, 10L));
        verify(projection, never()).add(any());
    }

    @Test
    void missingDormitoryDoesNotUpdateProjection() {
        listener.onAuditLogRecorded(event(AuditAction.DOCUMENT_UPLOADED, null));
        verify(projection, never()).add(any());
    }

    private AuditLogRecordedEvent event(AuditAction action, Long dormitoryId) {
        return new AuditLogRecordedEvent(1L, 2L, "Student One", Role.STUDENT,
                3L, "Student One", dormitoryId, "Dormitory",
                AuditCategory.STUDENT_ACTIVITY, action, AuditEntityType.STUDENT_DOCUMENT,
                4L, "Identity Document", "description",
                LocalDateTime.of(2026, 8, 12, 10, 0));
    }
}
