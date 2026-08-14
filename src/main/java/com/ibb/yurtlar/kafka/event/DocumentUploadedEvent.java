package com.ibb.yurtlar.kafka.event;

import java.time.LocalDateTime;

public record DocumentUploadedEvent(
        String eventId,
        String eventType,
        Long studentId,
        Long admissionId,
        Long documentId,
        Long documentTypeId,
        Long dormitoryId,
        LocalDateTime occurredAt
) {
}