package com.ibb.yurtlar.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateDormitoryTermActiveRequest(

        @NotNull(message = "Aktiflik bilgisi zorunludur.")
        Boolean active

) {
}