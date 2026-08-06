package com.ibb.yurtlar.dto;

public record AdmissionStatusCountResponse(

        long totalCount,
        long pendingCount,
        long approvedCount,
        long rejectedCount

) {
}