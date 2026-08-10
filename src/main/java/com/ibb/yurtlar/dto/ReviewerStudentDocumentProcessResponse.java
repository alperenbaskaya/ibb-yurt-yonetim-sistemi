package com.ibb.yurtlar.dto;

import java.util.List;

public record ReviewerStudentDocumentProcessResponse(
        Long studentId,
        String firstName,
        String lastName,
        String identityNumber,
        Long dormitoryTermId,
        String dormitoryTermName,
        int totalRequiredDocumentCount,
        int approvedRequiredDocumentCount,
        int missingRequiredDocumentCount,
        int uploadedRequiredDocumentCount,
        int revisionRequiredDocumentCount,
        int rejectedDocumentCount,
        boolean completed,
        List<ReviewerStudentDocumentProcessItemResponse> requiredDocuments
) {
}
