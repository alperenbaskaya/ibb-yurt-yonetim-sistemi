package com.ibb.yurtlar.dto;

public record DormitoryReviewerWorkloadResponse(

        Long reviewerId,
        String firstName,
        String lastName,
        String email,
        boolean active,

        long approvedReviewCount,
        long rejectedReviewCount,
        long revisionRequiredReviewCount,
        long totalReviewCount

) {
}