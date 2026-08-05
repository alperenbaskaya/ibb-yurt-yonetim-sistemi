package com.ibb.yurtlar.dto;

import java.time.LocalDateTime;

public record TermDocumentRequirementResponse(

        Long id,

        Long dormitoryTermId,
        String dormitoryTermName,

        Long documentTypeId,
        String documentTypeName,
        String documentTypeDescription,

        boolean documentTypeActive,
        boolean required,

        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}