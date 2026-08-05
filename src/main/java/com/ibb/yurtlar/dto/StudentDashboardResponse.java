package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.AdmissionStatus;

import java.util.List;

public record StudentDashboardResponse(

        Long studentId,
        Long userId,

        String firstName,
        String lastName,
        String email,

        Long admissionId,
        AdmissionStatus admissionStatus,

        Long dormitoryId,
        String dormitoryName,

        Long dormitoryTermId,
        String dormitoryTermName,

        int totalRequiredDocuments,
        int approvedRequiredDocuments,
        boolean documentProcessCompleted,

        List<StudentDocumentRequirementStatusResponse> documents

) {
}