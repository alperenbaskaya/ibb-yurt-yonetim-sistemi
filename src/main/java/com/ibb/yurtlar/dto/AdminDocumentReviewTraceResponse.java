package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.DocumentReviewDecision;

import java.time.LocalDateTime;

public record AdminDocumentReviewTraceResponse(
        Long reviewerUserId,
        String reviewerFirstName,
        String reviewerLastName,
        DocumentReviewDecision decision,
        String comment,
        LocalDateTime reviewedAt
) {
}
