package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.StudentDocumentStatus;

public record ReviewerStudentDocumentProcessItemResponse(
        Long documentTypeId,
        String documentTypeName,
        boolean uploaded,
        StudentDocumentStatus status
) {
}
