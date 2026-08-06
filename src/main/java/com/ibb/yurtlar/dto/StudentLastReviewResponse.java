package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.DocumentReviewDecision;

import java.time.LocalDateTime;

public record StudentLastReviewResponse(

        Long reviewId,

        Long studentDocumentId,

        Long documentTypeId,
        String documentTypeName,

        DocumentReviewDecision decision,
        String comment,
        LocalDateTime reviewedAt

) {
}
