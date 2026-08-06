package com.ibb.yurtlar.dto;

import java.util.List;

public record DormitoryAdminDashboardResponse(

        Long adminId,
        String firstName,
        String lastName,
        String email,

        Long dormitoryId,
        String dormitoryName,

        Long activeTermId,
        String activeTermName,

        int capacity,
        long activeStudentCount,
        long availableCapacity,
        int occupancyPercentage,

        long totalAdmissionCount,
        long pendingAdmissionCount,
        long approvedAdmissionCount,
        long rejectedAdmissionCount,

        long completedStudentCount,
        long incompleteStudentCount,
        int studentCompletionPercentage,

        long totalReviewerCount,
        long activeReviewerCount,
        long inactiveReviewerCount,

        long pendingDocumentCount,
        long approvedDocumentCount,
        long rejectedDocumentCount,
        long revisionRequiredDocumentCount,

        List<DormitoryReviewerWorkloadResponse>
                reviewerWorkloads,

        List<PendingDocumentTypeCountResponse>
                pendingDocumentsByType,

        List<ActionRequiredStudentResponse>
                actionRequiredStudents

) {
}