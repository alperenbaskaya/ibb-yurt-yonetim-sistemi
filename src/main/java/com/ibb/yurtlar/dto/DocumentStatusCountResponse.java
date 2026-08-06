package com.ibb.yurtlar.dto;

public record DocumentStatusCountResponse(

        long pendingCount,
        long approvedCount,
        long rejectedCount,
        long revisionRequiredCount

) {
}