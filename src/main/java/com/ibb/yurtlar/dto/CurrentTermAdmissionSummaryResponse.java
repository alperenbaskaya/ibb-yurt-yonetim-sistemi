package com.ibb.yurtlar.dto;

public record CurrentTermAdmissionSummaryResponse(

        Long dormitoryTermId,
        String dormitoryTermName,

        long totalAdmissions,
        long pendingAdmissions,
        long approvedAdmissions,
        long rejectedAdmissions

) {
}