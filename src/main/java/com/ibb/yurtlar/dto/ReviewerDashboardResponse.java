package com.ibb.yurtlar.dto;

import java.util.List;

public record ReviewerDashboardResponse(

        Long reviewerId,
        String firstName,
        String lastName,
        String email,

        Long dormitoryId,
        String dormitoryName,

        Long activeTermId,
        String activeTermName,

        long activeStudentCount,
        long completedStudentCount,
        long incompleteStudentCount,
        long actionRequiredStudentCount,
        int studentCompletionPercentage,

        long pendingDocumentCount,
        long approvedDocumentCount,
        long rejectedDocumentCount,
        long revisionRequiredDocumentCount,

        List<PendingDocumentTypeCountResponse>
                pendingDocumentsByType,

        List<StudentDocumentResponse>
                oldestPendingDocuments,

        List<ActionRequiredStudentResponse>
                actionRequiredStudents,

        List<DocumentReviewResponse>
                recentDormitoryReviews

) {
}
