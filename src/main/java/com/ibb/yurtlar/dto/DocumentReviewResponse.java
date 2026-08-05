package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.DocumentReviewDecision;

import java.time.LocalDateTime;

public record DocumentReviewResponse(

        Long id,

        Long studentDocumentId,
        String documentTypeName,
        String originalFileName,

        Long studentId,
        String studentFirstName,
        String studentLastName,

        Long reviewerUserId,
        String reviewerFirstName,
        String reviewerLastName,

        DocumentReviewDecision decision,
        String comment,
        LocalDateTime reviewedAt

) {
}