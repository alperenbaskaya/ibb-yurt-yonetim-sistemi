package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.StudentDocumentStatus;

import java.time.LocalDateTime;
import java.util.List;

public record AdminStudentDocumentInspectionItemResponse(
        Long documentTypeId,
        String documentTypeName,
        boolean required,
        boolean uploaded,
        Long studentDocumentId,
        StudentDocumentStatus status,
        String originalFileName,
        LocalDateTime uploadedAt,
        AdminDocumentReviewTraceResponse latestReview,
        List<AdminDocumentReviewTraceResponse> reviewHistory
) {
}
