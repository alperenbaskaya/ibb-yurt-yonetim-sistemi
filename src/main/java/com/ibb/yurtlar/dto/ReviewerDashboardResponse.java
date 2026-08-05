package com.ibb.yurtlar.dto;

import java.util.List;

public record ReviewerDashboardResponse(

        Long reviewerId,
        String firstName,
        String lastName,
        String email,

        long pendingDocumentCount,
        long approvedReviewCount,
        long rejectedReviewCount,
        long revisionRequiredReviewCount,

        List<StudentDocumentResponse> oldestPendingDocuments,
        List<DocumentReviewResponse> recentReviews

) {
}