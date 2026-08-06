package com.ibb.yurtlar.dto;

import java.util.List;

public record GlobalAdminDashboardResponse(

        Long adminId,
        String firstName,
        String lastName,
        String email,

        Long activeTermId,
        String activeTermName,

        long totalDormitoryCount,
        long activeDormitoryCount,
        long inactiveDormitoryCount,

        long totalCapacity,
        long activeStudentCount,
        long availableCapacity,
        int occupancyPercentage,

        long totalAdmissionCount,
        long pendingAdmissionCount,
        long approvedAdmissionCount,
        long rejectedAdmissionCount,

        long completedStudentCount,
        long incompleteStudentCount,
        long actionRequiredStudentCount,
        int studentCompletionPercentage,

        long totalAdminCount,
        long globalAdminCount,
        long dormitoryAdminCount,

        long totalReviewerCount,
        long activeReviewerCount,
        long inactiveReviewerCount,

        long totalStudentUserCount,

        long pendingDocumentCount,
        long approvedDocumentCount,
        long rejectedDocumentCount,
        long revisionRequiredDocumentCount,

        long admissionCompletedDormitoryCount,
        long admissionInProgressDormitoryCount,

        List<GlobalDormitoryStatisticsResponse>
                dormitoryStatistics

) {
}