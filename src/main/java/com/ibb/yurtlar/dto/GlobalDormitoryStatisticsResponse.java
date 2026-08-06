package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.DormitoryAdmissionProcessStatus;

import java.util.List;

public record GlobalDormitoryStatisticsResponse(

        Long dormitoryId,
        String dormitoryName,
        boolean active,

        GlobalDormitoryManagerResponse manager,

        long totalReviewerCount,
        long activeReviewerCount,
        long inactiveReviewerCount,

        List<GlobalDormitoryReviewerResponse>
                reviewers,

        int capacity,
        long activeStudentCount,
        long availableCapacity,
        int occupancyPercentage,

        long totalAdmissionCount,
        long pendingAdmissionCount,
        long approvedAdmissionCount,
        long rejectedAdmissionCount,

        DormitoryAdmissionProcessStatus
                admissionProcessStatus,

        boolean admissionProcessCompleted,
        String admissionProcessMessage,

        long completedStudentCount,
        long incompleteStudentCount,
        long actionRequiredStudentCount,
        int studentCompletionPercentage,

        long pendingDocumentCount,
        long approvedDocumentCount,
        long rejectedDocumentCount,
        long revisionRequiredDocumentCount

) {
}