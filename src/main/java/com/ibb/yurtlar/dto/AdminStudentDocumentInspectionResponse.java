package com.ibb.yurtlar.dto;

import java.util.List;

public record AdminStudentDocumentInspectionResponse(
        Long studentId,
        String studentFirstName,
        String studentLastName,
        String identityNumber,
        Long dormitoryId,
        String dormitoryName,
        Long dormitoryTermId,
        String dormitoryTermName,
        int totalRequiredDocumentCount,
        int approvedRequiredDocumentCount,
        int missingRequiredDocumentCount,
        int uploadedRequiredDocumentCount,
        int revisionRequiredDocumentCount,
        int rejectedDocumentCount,
        boolean completed,
        List<AdminStudentDocumentInspectionItemResponse> requiredDocuments
) {
}
