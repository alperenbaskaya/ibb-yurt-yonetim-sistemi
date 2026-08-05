package com.ibb.yurtlar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDocumentTypeRequest(

        @NotBlank(message = "Belge türü adı boş olamaz.")
        @Size(
                max = 150,
                message = "Belge türü adı en fazla 150 karakter olabilir."
        )
        String name,

        @Size(
                max = 500,
                message = "Açıklama en fazla 500 karakter olabilir."
        )
        String description,

        boolean active

) {
}