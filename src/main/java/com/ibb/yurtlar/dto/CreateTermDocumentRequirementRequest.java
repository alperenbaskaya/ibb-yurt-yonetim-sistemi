package com.ibb.yurtlar.dto;

import jakarta.validation.constraints.NotNull;

public record CreateTermDocumentRequirementRequest(

        @NotNull(message = "Yurt dönemi ID değeri zorunludur.")
        Long dormitoryTermId,

        @NotNull(message = "Belge türü ID değeri zorunludur.")
        Long documentTypeId,

        boolean required

) {
}