package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.StudentDocumentStatus;

import java.time.LocalDateTime;

public record StudentDocumentRequirementStatusResponse(

        Long requirementId,

        Long documentTypeId,
        String documentTypeName,
        String documentTypeDescription,

        boolean required,

        boolean uploaded,

        Long studentDocumentId,
        String originalFileName,
        StudentDocumentStatus status,
        LocalDateTime uploadedAt

) {
}