package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.DocumentReviewDecision;

import java.time.LocalDateTime;

public record StudentProcessTimelineItemResponse(
        String key,
        AuditAction action,
        Long documentTypeId,
        String documentTypeName,
        String description,
        LocalDateTime createdAt,
        DocumentReviewDecision reviewDecision,
        String reviewComment
) {
}
