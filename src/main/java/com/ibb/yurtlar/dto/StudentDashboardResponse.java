package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.AdmissionStatus;
import com.ibb.yurtlar.enums.UploadPeriodStatus;

import java.time.LocalDate;
import java.util.List;

public record StudentDashboardResponse(

        Long studentId,
        Long userId,

        String firstName,
        String lastName,
        String email,

        Long admissionId,
        AdmissionStatus admissionStatus,
        String admissionStatusMessage,

        Long dormitoryId,
        String dormitoryName,

        Long dormitoryTermId,
        String dormitoryTermName,

        LocalDate documentUploadStartDate,
        LocalDate documentUploadEndDate,
        long remainingUploadDays,
        UploadPeriodStatus uploadPeriodStatus,

        int totalRequiredDocuments,
        int approvedRequiredDocuments,
        int completionPercentage,
        boolean documentProcessCompleted,

        StudentLastReviewResponse lastReview,

        List<StudentDocumentActionRequiredResponse>
                actionRequiredDocuments,

        List<StudentDocumentRequirementStatusResponse>
                documents

) {
}