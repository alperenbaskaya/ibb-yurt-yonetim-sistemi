package com.ibb.yurtlar.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateTermDocumentRequirementRequest(

        @NotNull(message = "Zorunluluk bilgisi belirtilmelidir.")
        Boolean required

) {
}