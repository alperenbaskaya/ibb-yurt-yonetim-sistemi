package com.ibb.yurtlar.dto;

import java.util.List;

public record ReviewerDashboardResponse(

        Long reviewerId,
        String firstName,
        String lastName,
        String email,

        Long dormitoryId,
        String dormitoryName,

        long pendingDocumentCount,
        long approvedDocumentCount,
        long rejectedDocumentCount,
        long revisionRequiredDocumentCount,

        List<StudentDocumentResponse> oldestPendingDocuments,
        List<DocumentReviewResponse> recentDormitoryReviews

) {
}