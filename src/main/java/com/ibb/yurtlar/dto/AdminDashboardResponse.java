package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.AdminScope;

import java.util.List;

public record AdminDashboardResponse(

        Long adminUserId,
        String adminFirstName,
        String adminLastName,

        AdminScope adminScope,

        Long dormitoryId,
        String dormitoryName,

        Long activeTermId,
        String activeTermName,

        long totalUsers,
        long adminUserCount,
        long reviewerUserCount,
        long studentUserCount,
        long studentProfileCount,

        long totalAdmissions,
        long pendingAdmissions,
        long approvedAdmissions,
        long rejectedAdmissions,

        long pendingDocumentCount,
        long approvedDocumentCount,
        long rejectedDocumentCount,
        long revisionRequiredDocumentCount,

        List<AdminRecentAdmissionResponse> recentAdmissions

) {
}