package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.StudentDocumentActionReason;

public record StudentDocumentActionRequiredResponse(

        Long documentTypeId,
        String documentTypeName,

        Long studentDocumentId,

        StudentDocumentActionReason reason,
        String message

) {
}